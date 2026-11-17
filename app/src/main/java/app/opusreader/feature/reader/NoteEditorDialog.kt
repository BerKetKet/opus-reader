package app.opusreader.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import app.opusreader.design.theme.OpusColors
import app.opusreader.design.theme.OpusDimensions
import app.opusreader.design.theme.OpusTypography

@Composable
fun NoteEditorDialog(
    initialText: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(OpusDimensions.CornerRadiusSmall))
                .background(OpusColors.SurfaceDark)
                .border(
                    width = OpusDimensions.DividerThickness,
                    color = OpusColors.BorderDark,
                    shape = RoundedCornerShape(OpusDimensions.CornerRadiusSmall)
                )
                .padding(OpusDimensions.SpacingLarge)
                .testTag("note_editor_dialog")
        ) {
            Text(
                text = "Note",
                style = OpusTypography.titleLarge,
                color = OpusColors.TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingMedium))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        text = "Enter note text...",
                        style = OpusTypography.bodyMedium,
                        color = OpusColors.TextSecondaryDark
                    )
                },
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OpusColors.TextPrimaryDark,
                    unfocusedTextColor = OpusColors.TextPrimaryDark,
                    focusedContainerColor = OpusColors.SurfaceVariantDark,
                    unfocusedContainerColor = OpusColors.SurfaceVariantDark,
                    focusedBorderColor = OpusColors.AccentBlue,
                    unfocusedBorderColor = OpusColors.BorderDark,
                    cursorColor = OpusColors.AccentBlue
                ),
                textStyle = OpusTypography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_editor_input")
            )

            Spacer(modifier = Modifier.height(OpusDimensions.SpacingLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CANCEL",
                    style = OpusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OpusColors.LinkBlueDark,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = OpusDimensions.SpacingSmall, vertical = OpusDimensions.SpacingXXSmall)
                        .testTag("note_editor_cancel")
                )
                Spacer(modifier = Modifier.padding(horizontal = OpusDimensions.SpacingSmall))
                Text(
                    text = "SAVE",
                    style = OpusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OpusColors.LinkBlueDark,
                    modifier = Modifier
                        .clickable { onSave(text) }
                        .padding(horizontal = OpusDimensions.SpacingSmall, vertical = OpusDimensions.SpacingXXSmall)
                        .testTag("note_editor_save")
                )
            }
        }
    }
}
