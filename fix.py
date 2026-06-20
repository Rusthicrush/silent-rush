import re

def patch(path, old, new, label):
    with open(path, "r") as f:
        content = f.read()
    count = content.count(old)
    if count == 0:
        print(f"SKIP ({label}): pattern not found in {path}")
        return
    content = content.replace(old, new)
    with open(path, "w") as f:
        f.write(content)
    print(f"OK ({label}): {path} [{count} replaced]")

patch("app/build.gradle.kts",
    "  // implementation(libs.androidx.compose.material.icons.extended)",
    "  implementation(libs.androidx.compose.material.icons.extended)",
    "enable extended icons")

patch("app/src/main/java/com/example/MainActivity.kt",
    "    currentTab = HomeTab.HOME,",
    "    currentTab: HomeTab,",
    "fix param type")

patch("app/src/main/java/com/example/playback/SilentRushPlayer.kt",
    '    private const val TAG = "SilentRushPlayer"',
    '    private val TAG = "SilentRushPlayer"',
    "fix const val")

patch("app/src/main/java/com/example/ui/SilentRushViewModel.kt",
    '    private const val TAG = "SilentRushViewModel"',
    '    private val TAG = "SilentRushViewModel"',
    "fix const val")

patch("app/src/main/java/com/example/ui/EqualizerView.kt",
    "import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.shape.RoundedCornerShape",
    "import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.shape.RoundedCornerShape",
    "add items import")

patch("app/src/main/java/com/example/ui/NowPlayingView.kt",
    "Icons.Outline.Timer",
    "Icons.Outlined.Timer",
    "fix Outline typo")

patch("app/src/main/java/com/example/ui/NowPlayingView.kt",
    "RepeatMode.Restart",
    "androidx.compose.animation.core.RepeatMode.Restart",
    "fix RepeatMode clash")

patch("app/src/main/java/com/example/ui/NowPlayingView.kt",
    "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.drawscope.Stroke",
    "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.Path\nimport androidx.compose.ui.graphics.drawscope.Stroke",
    "add Path import")

patch("app/src/main/java/com/example/ui/PlaylistView.kt",
    "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.*",
    "import androidx.compose.foundation.BorderStroke\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.*",
    "add BorderStroke/background")

patch("app/src/main/java/com/example/ui/PlaylistView.kt",
    "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp",
    "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextOverflow\nimport androidx.compose.ui.unit.dp",
    "add TextOverflow")

patch("app/src/main/java/com/example/ui/SearchView.kt",
    "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.*",
    "import androidx.compose.foundation.BorderStroke\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.*",
    "add BorderStroke/background")

print("done")
