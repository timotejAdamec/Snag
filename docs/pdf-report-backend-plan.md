# PDF Report Backend — Implementation Plan

## Overview

Add the ability to generate a PDF report for a project. The report aggregates data
from projects, clients, structures, findings, and inspections into a downloadable
PDF document. Backend-only for now.

---

## 1. PDF Library Choice

**Recommended: OpenPDF** (fork of iText 4)

| Criteria        | OpenPDF                          | Apache PDFBox                  |
|-----------------|----------------------------------|--------------------------------|
| License         | LGPL + MPL                       | Apache 2.0                     |
| API level       | High-level (Document, Table)     | Low-level (page streams)       |
| Tables          | Built-in `PdfPTable`             | Manual coordinate layout       |
| Images          | Built-in `Image.getInstance()`   | `PDImageXObject` + manual draw |
| Font handling   | Simple via `BaseFont`            | More manual                    |
| Maintenance     | Active                           | Active                         |

OpenPDF is the better fit here because the report is a structured document with
tables, headings, images, and text. Its high-level Document API maps directly to
what we need. PDFBox would require manually computing coordinates for every element.

**Dependency to add to version catalog:**

```toml
[versions]
openpdf = "2.0.3"

[libraries]
openpdf = { module = "com.github.librepdf:openpdf", version.ref = "openpdf" }
```

---

## 2. Report Content

### Page layout
- A4 portrait, reasonable margins

### Sections

1. **Cover page**
   - Project name (large heading)
   - Project address
   - Client name, address, email, phone (if client is linked)
   - Report generation date

2. **Inspections section**
   - One table listing all inspections for the project
   - Columns: Date range (startedAt – endedAt), Participants, Climate, Note

3. **Structure sections** (one per structure, sorted by name)
   - Structure name as section heading
   - Floor plan image with finding markers overlaid (if `floorPlanUrl` is present)
     - Download the image via URL
     - Draw numbered markers at the relative coordinates scaled to image dimensions
   - Findings table for this structure
     - Columns: # (marker number), Name, Description, Type, Importance, Term
     - Only Classic findings have Importance and Term; Unvisited and Note show their type

4. **Summary page**
   - Total findings count
   - Breakdown by importance (HIGH / MEDIUM / LOW)
   - Breakdown by type (Classic / Unvisited / Note)
   - Breakdown by term (T1 / T2 / T3 / CON)

---

## 3. Architecture — Module Structure

Following the project's hexagonal architecture. The report feature is cross-cutting
(aggregates data from projects, clients, structures, findings, inspections), so it
lives in its own feature directory.

```
feat/reports/
├── business/             # Report domain model (projectId, url)
├── be/
│   ├── model/            # BackendReport (wraps Report + PDF bytes)
│   ├── app/
│   │   ├── api/          # GenerateProjectReportUseCase interface
│   │   └── impl/         # Use case impl — orchestrates data fetching + PDF generation
│   ├── ports/            # PdfReportGenerator port interface + report data model
│   ├── driven/
│   │   └── impl/         # OpenPDF implementation of PdfReportGenerator
│   └── driving/
│       └── impl/         # Ktor route: GET /projects/{projectId}/report
```

### Module details

#### `feat/reports/business`
- Plugin: `snagBusinessModule`
- Contains:
  - `Report` — domain model representing a generated report

```kotlin
data class Report(
    val projectId: Uuid,
    val url: String,
)
```

#### `feat/reports/be/model`
- Plugin: `snagBackendModule`
- Contains:
  - `BackendReport` — wraps the business `Report` with the actual PDF bytes

```kotlin
data class BackendReport(
    val report: Report,
    val pdfBytes: ByteArray,
)
```

#### `feat/reports/be/ports`
- Plugin: `snagBackendModule`
- Contains:
  - `ProjectReportData` — aggregate data class holding all data needed for the report
  - `StructureWithFindings` — pairs a structure with its findings
  - `PdfReportGenerator` — port interface

```kotlin
data class ProjectReportData(
    val project: Project,
    val client: Client?,
    val structures: List<StructureWithFindings>,
    val inspections: List<Inspection>,
    val generatedAt: Timestamp,
)

data class StructureWithFindings(
    val structure: Structure,
    val findings: List<Finding>,
)

interface PdfReportGenerator {
    suspend fun generate(data: ProjectReportData): ByteArray
}
```

- Dependencies:
  - `:feat:projects:business`
  - `:feat:clients:business`
  - `:feat:structures:business`
  - `:feat:findings:business`
  - `:feat:inspections:business`

#### `feat/reports/be/app/api`
- Plugin: `snagBackendModule`
- Contains: `GenerateProjectReportUseCase` interface

```kotlin
interface GenerateProjectReportUseCase {
    suspend operator fun invoke(projectId: Uuid): BackendReport?
    // Returns null if project not found
}
```

- Dependencies (auto-wired by plugin): depends on ports module

#### `feat/reports/be/app/impl`
- Plugin: `snagBackendModule`
- Contains: `GenerateProjectReportUseCaseImpl`
- Responsibilities:
  1. Fetch the project via `GetProjectUseCase`; return null if not found
  2. Fetch the client via `GetClientUseCase` (if `project.clientId` is set)
  3. Fetch structures via `GetStructuresUseCase(projectId)`; filter out soft-deleted
  4. For each structure, fetch findings via `GetFindingsUseCase(structureId)`; filter out soft-deleted
  5. Fetch inspections via `GetInspectionsUseCase(projectId)`; filter out soft-deleted
  6. Assemble `ProjectReportData`
  7. Call `PdfReportGenerator.generate(data)` to get the PDF bytes
  8. Construct the file name (e.g. `"${project.name}_report.pdf"`)
  9. Return `BackendReport(Report(projectId, fileName), pdfBytes)`
- Dependencies:
  - `:feat:reports:be:app:api`
  - `:feat:reports:be:ports`
  - `:feat:projects:be:app:api`
  - `:feat:clients:be:app:api`
  - `:feat:structures:be:app:api`
  - `:feat:findings:be:app:api`
  - `:feat:inspections:be:app:api`

#### `feat/reports/be/driven/impl`
- Plugin: `snagBackendModule` (not `snagDrivenBackendModule` since this has no Exposed/DB dependency)
- Contains: `OpenPdfReportGenerator` implementing `PdfReportGenerator`
- Responsibilities:
  - Build the PDF document using OpenPDF's `Document`, `PdfWriter`, `PdfPTable`, `Image` APIs
  - Download floor plan images from their URLs (using a simple HTTP client or `java.net.URI`)
  - Draw numbered markers on floor plan images at finding coordinates
  - Render all sections described in Section 2 above
- Dependencies:
  - `:feat:reports:be:ports`
  - `libs.openpdf`

#### `feat/reports/be/driving/impl`
- Plugin: `snagImplDrivingBackendModule`
- Contains: `ReportRoute` implementing `AppRoute`
- Route definition:

```kotlin
internal class ReportRoute(
    private val generateProjectReportUseCase: GenerateProjectReportUseCase,
) : AppRoute {
    override fun Route.setup() {
        get("/projects/{projectId}/report") {
            val projectId = getIdFromParameters("projectId")
            val backendReport = generateProjectReportUseCase(projectId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Project not found.")

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName, backendReport.report.url
                ).toString()
            )
            call.respondBytes(backendReport.pdfBytes, ContentType.Application.Pdf)
        }
    }
}
```

- Dependencies:
  - `:feat:reports:be:app:api`

---

## 4. Dependency Injection (Koin)

Three new Koin modules, following the existing pattern:

```kotlin
// feat/reports/be/app/impl/.../di/ReportsAppModule.kt
val reportsAppModule = module {
    factoryOf(::GenerateProjectReportUseCaseImpl) bind GenerateProjectReportUseCase::class
}

// feat/reports/be/driven/impl/.../di/ReportsDrivenModule.kt
val reportsDrivenModule = module {
    singleOf(::OpenPdfReportGenerator) bind PdfReportGenerator::class
}

// feat/reports/be/driving/impl/.../di/ReportsDrivingModule.kt
val reportsDrivingModule = module {
    singleOf(::ReportRoute) bind AppRoute::class
}
```

Register all three in `BackendModulesAggregate.kt`:

```kotlin
includes(
    // ... existing modules ...
    reportsDrivingModule,
    reportsDrivenModule,
    reportsAppModule,
)
```

---

## 5. Gradle Module Registration

Add to `settings.gradle.kts`:

```kotlin
include(":feat:reports:business")
include(":feat:reports:be:model")
include(":feat:reports:be:app:api")
include(":feat:reports:be:app:impl")
include(":feat:reports:be:ports")
include(":feat:reports:be:driven:impl")
include(":feat:reports:be:driving:impl")
```

### build.gradle.kts files

**`feat/reports/business/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.snagBusinessModule)
}
```

**`feat/reports/be/model/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.snagBackendModule)
}
// Auto-wired: depends on business module via plugin convention
```

**`feat/reports/be/ports/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.snagBackendModule)
}
dependencies {
    api(project(":feat:projects:business"))
    api(project(":feat:clients:business"))
    api(project(":feat:structures:business"))
    api(project(":feat:findings:business"))
    api(project(":feat:inspections:business"))
}
```

**`feat/reports/be/app/api/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.snagBackendModule)
}
// Auto-wired: depends on ports via plugin convention
```

**`feat/reports/be/app/impl/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.snagBackendModule)
}
dependencies {
    implementation(project(":feat:reports:be:app:api"))
    implementation(project(":feat:reports:be:ports"))
    implementation(project(":feat:projects:be:app:api"))
    implementation(project(":feat:clients:be:app:api"))
    implementation(project(":feat:structures:be:app:api"))
    implementation(project(":feat:findings:be:app:api"))
    implementation(project(":feat:inspections:be:app:api"))
}
```

**`feat/reports/be/driven/impl/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.snagBackendModule)
}
dependencies {
    implementation(project(":feat:reports:be:ports"))
    implementation(libs.openpdf)
}
```

**`feat/reports/be/driving/impl/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.snagImplDrivingBackendModule)
}
dependencies {
    implementation(project(":feat:reports:be:app:api"))
}
```

---

## 6. Floor Plan Image Handling

Structures may have a `floorPlanUrl` pointing to an image in GCS. For the PDF:

1. In `OpenPdfReportGenerator`, download the image bytes from the URL
2. Load as `com.lowagie.text.Image`
3. Scale to fit the page width while maintaining aspect ratio
4. For each finding with coordinates, draw a numbered circle marker on the image:
   - Use `PdfContentByte` from the `PdfWriter` to draw directly over the image
   - Map `RelativeCoordinate(x, y)` to absolute position:
     `absoluteX = imageX + x * imageWidth`, `absoluteY = imageY + (1 - y) * imageHeight`
   - Draw a small filled circle with the finding's index number
5. If `floorPlanUrl` is null, skip the floor plan and just show the findings table

---

## 7. Implementation Order

1. **Add OpenPDF to version catalog** (`gradle/libs.versions.toml`)
2. **Register modules** in `settings.gradle.kts`
3. **`feat/reports/business`** — `Report` domain model
4. **`feat/reports/be/model`** — `BackendReport`
5. **`feat/reports/be/ports`** — report data model + `PdfReportGenerator` interface
6. **`feat/reports/be/app/api`** — `GenerateProjectReportUseCase` interface
7. **`feat/reports/be/app/impl`** — use case implementation (data aggregation + fileName construction)
8. **`feat/reports/be/driven/impl`** — OpenPDF implementation of `PdfReportGenerator`
   - Start with basic text/tables, then add floor plan rendering
9. **`feat/reports/be/driving/impl`** — HTTP route
10. **DI wiring** — Koin modules + registration in `BackendModulesAggregate`
11. **Tests**
   - Use case test: mock ports, verify correct data aggregation
   - Route test: verify endpoint returns PDF bytes with correct content type
   - PDF generator test: verify output is valid PDF (can parse with PDFBox in test)

---

## 8. API Contract

```
GET /projects/{projectId}/report

Response 200:
  Content-Type: application/pdf
  Content-Disposition: attachment; filename="{project_name}_report.pdf"
  Body: <PDF bytes>

Response 404:
  Body: "Project not found."
```

---

## 9. Open Questions / Future Considerations

- **Locale/language**: If the app needs to support multiple languages in reports,
  the PDF text strings would need to be parameterized. For now, assume a single language.
- **Caching**: Reports are generated on the fly. If generation becomes slow for
  large projects, consider caching or async generation. Not needed initially.
- **Frontend trigger**: Eventually the frontend will need a button/screen to
  request and download the report. Out of scope for this plan.
- **Report customization**: Could add query parameters to select which sections
  to include, date range filters, etc. Keep it simple for v1 — always generate
  the full report.
