# Standardize hide vs disable pattern for UI elements

## Problem

The codebase inconsistently uses **hiding** (conditional rendering) vs **disabling** (`enabled = false`) for similar scenarios, particularly around permission-based and state-based controls.

## Current State

### Elements that are **hidden** (conditional `if`):

| Condition | Element | Location |
|---|---|---|
| `canCreateProject` | "Create project" button | `ProjectsContent.kt:49` |
| `canAssignUsers` | "Manage" button | `ProjectDetailsContent.kt:244` |
| `canAssignUsers` | Trailing icon on chips | `ProjectDetailsContent.kt:273` |
| `isProjectEditable` | "Create inspection/structure" buttons | `ProjectDetailsContent.kt:321,379` |
| `canManageAssignments` | "Add user" button | `ProjectAssignmentsContent.kt:77` |
| `canRemove` | Delete icon | `AssignedUserListItem.kt:60` |
| `canEdit` | Add photo button | `FindingPhotoSection.kt:106` |
| `canEdit` | Delete button on thumbnails | `FindingPhotoSection.kt:185` |
| `isEditMode` | Delete button | `InspectionEditContent.kt:257` |

### Elements that are **disabled** (`enabled = false`):

| Condition | Element | Location |
|---|---|---|
| `canSave` | Save button | `ProjectDetailsEditContent.kt:102`, `StructureDetailsEditContent.kt:101` |
| `isProjectEditable` | Inspection card actions | `ProjectDetailsContent.kt:351` |
| `canToggleClosed` | Close/reopen button | `ProjectDetailsContent.kt:467` |
| `canDownloadReport` | Export button | `ProjectDetailsContent.kt:505` |
| `canInvokeDeletion` | Delete button | `ProjectDetailsContent.kt:520` |
| `canEdit` | Edit & delete buttons | `FindingDetailContent.kt:258,267` |
| `canEdit` | Delete, save, and all fields | `InspectionEditContent.kt:259,270,295+` |
| `canDelete` | Delete button | `ClientDetailsEditContent.kt:126` |

## Key Inconsistencies

1. **`isProjectEditable`** hides create buttons (lines 321, 379) but disables inspection card actions (line 351) — in the same file.
2. **`canEdit`** hides photo add/delete in `FindingPhotoSection` but disables edit/delete in `FindingDetailContent` and fields in `InspectionEditContent`.
3. **Permission checks** like `canAssignUsers`, `canCreateProject`, `canRemove` use hiding, while similar checks like `canToggleClosed`, `canInvokeDeletion`, `canDownloadReport` use disabling.

## M3 Guidelines

Per Material Design 3 interaction states and UX best practices:

| Condition | Recommended Approach |
|---|---|
| **Temporary unavailability** (loading, incomplete form, in-progress action) | **Disable** — user can take action to enable it |
| **Role-based permanent** (user's role will never grant access) | **Hide** — reduces cognitive load |
| **State-based reversible** (e.g. project closed but can be reopened) | **Disable** + communicate why |
| **Contextual irrelevance** (element doesn't apply in current mode) | **Hide** |

## Proposed Policy

1. **Hide** when the user will **never** have access (role/permission-based).
2. **Disable** when the state is **temporary or reversible** (form validation, project closed, loading).
3. When disabling, provide context (helper text / tooltip) explaining why and how to enable.
4. Audit all existing patterns and align them with this policy.

## Action Items

- [ ] Decide on the policy (confirm or adjust the proposal above)
- [ ] Audit each instance and categorize the condition as permanent vs temporary
- [ ] Align each element's behavior with the chosen policy
- [ ] Add helper text/tooltips for disabled elements where the reason isn't obvious
