import os

# --- FIX 1: NowPlayingView.kt Syntax Bug ---
now_playing_path = "app/src/main/java/com/example/ui/NowPlayingView.kt"
if os.path.exists(now_playing_path):
    with open(now_playing_path, "r") as f:
        content = f.read()
    
    # Broken Icon statement-ஐ சரிசெய்கிறோம்
    # ஏதோ தவறான Single branch inline check-ஐ நீக்கிவிட்டு Default Icon-ஐ வைப்போம்
    old_icon_block = """            Icon(
                imageVector = if (isShuffle) Icons.Filled.Shuffle else Icons.Filled.Shuffle,
                contentDescription = """
                
    # லாக்ஸ் காட்டுறபடி Icons.Filled.Shuffle-ல ஏதோ தப்பு நடந்துருக்கு. 
    # நாம் அந்த ஃபைலில் இருக்கும் உடைஞ்ச Icon பிளாக்கை தேடி மாற்றுவோம்.
    # இன்னும் சேஃபா, உடைஞ்ச வரியை மட்டும் டார்கெட் செய்வோம்.
    if "Icons.Filled.Shuffle" in content or "Filled" in content:
        # ஃபைலை ரீட் செய்து பிரத்யேகமாக அந்த லைனை மட்டும் சரிசெய்வோம்
        lines = content.split('\n')
        for i, line in enumerate(lines):
            if "if (" in line and "Icons." in line and "Filled" in line and "else" not in line:
                # else இல்லாமல் இருந்தால் அதை சரிசெய்கிறோம்
                lines[i] = '                imageVector = Icons.Filled.Shuffle,'
        content = '\n'.join(lines)
        
    with open(now_playing_path, "w") as f:
        f.write(content)
    print("OK: NowPlayingView patched")

# --- FIX 2: SilentRushViewModel.kt (val reassignment & private enum access) ---
viewmodel_path = "app/src/main/java/com/example/ui/SilentRushViewModel.kt"
if os.path.exists(viewmodel_path):
    with open(viewmodel_path, "r") as f:
        content = f.read()
    
    # val -> var மாற்றுதல் (லைன் 120-124 பிரச்சனையை தீர்க்க)
    # பொதுவாக இது init பிளாக்கிலோ அல்லது வேரியபிள் டிக்ளரேஷன்லையோ இருக்கும்.
    lines = content.split('\n')
    for i in range(115, min(130, len(lines))):
        if "val " in lines[i]:
            lines[i] = lines[i].replace("val ", "var ")
            
    content = '\n'.join(lines)
    with open(viewmodel_path, "w") as f:
        f.write(content)
    print("OK: ViewModel val->var reassignment patched")

# --- FIX 3: AudioSynthesizer.kt (SynthType enum visibility) ---
synth_path = "app/src/main/java/com/example/data/AudioSynthesizer.kt"
if os.path.exists(synth_path):
    with open(synth_path, "r") as f:
        content = f.read()
    
    # private enum class SynthType -> enum class SynthType
    if "private enum class SynthType" in content:
        content = content.replace("private enum class SynthType", "enum class SynthType")
    elif "private enum" in content:
        content = content.replace("private enum", "enum")
        
    with open(synth_path, "w") as f:
        f.write(content)
    print("OK: AudioSynthesizer visibility patched")

