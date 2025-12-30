package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.SnagTheme
import cz.adamec.timotej.snag.projects.business.Project

@Composable
internal fun ProjectCard(
    modifier: Modifier = Modifier,
    project: Project,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                ),
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            Text(
                text = project.client.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
@Preview
internal fun ProjectCardPreview() {
    SnagTheme {

    }
}
