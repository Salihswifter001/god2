import os
import re

# Dosyalar ve düzeltmeler
fixes = {
    "src/main/java/com/settings/MainActivity.kt": [
        (r': OctaApplication', ''),
        (r'as OctaApplication', 'as Any // OctaApplication kaldırıldı'),
    ],
    "src/main/java/com/ui/auth/ModernLoginScreen.kt": [
        (r'as OctaApplication', 'as Any // OctaApplication kaldırıldı'),
    ],
    "src/main/java/com/ui/auth/ModernSignupScreen.kt": [
        (r'as OctaApplication', 'as Any // OctaApplication kaldırıldı'),
    ],
    "src/main/java/com/ui/auth/OctaAILoginScreen.kt": [
        (r'as OctaApplication', 'as Any // OctaApplication kaldırıldı'),
    ]
}

for filepath, patterns in fixes.items():
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        for pattern, replacement in patterns:
            content = re.sub(pattern, replacement, content)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
            
        print(f"Fixed: {filepath}")
        
    except Exception as e:
        print(f"Error fixing {filepath}: {e}")