package dc.stashguard.core.ui

import androidx.compose.ui.graphics.Color
import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.model.Category

// The domain layer stores colors as ARGB ints; these give the Compose Color for display.

val Account.color: Color get() = Color(colorArgb)

val Category.color: Color get() = Color(colorArgb)
