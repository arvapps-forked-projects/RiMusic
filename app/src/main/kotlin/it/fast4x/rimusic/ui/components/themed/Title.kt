package it.fast4x.rimusic.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.semiBold

@Composable
fun Title(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = R.drawable.arrow_forward,
    onClick: (() -> Unit)? = null,
) {
    val (colorPalette, typography) = LocalAppearance.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = typography.l.semiBold.fontSize,
                fontWeight = typography.l.semiBold.fontWeight,
                color = colorPalette.text,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier.weight(1f)

        )

        if (onClick != null) {
            Icon(
                painter = painterResource(icon ?: R.drawable.arrow_forward),
                contentDescription = null,
                tint = colorPalette.text
            )
        }
    }
}

@Composable
fun TitleSection(
    title: String,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    Text(
        text = title,
        style = TextStyle(
            fontSize = typography.xl.bold.fontSize,
            fontWeight = typography.xl.bold.fontWeight,
            color = colorPalette.text,
            textAlign = TextAlign.Start
        ),
        modifier = modifier.padding(end = 12.dp)

    )


}
