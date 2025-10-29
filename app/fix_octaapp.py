import os
import re

# Dosya listesi
files_to_fix = [
    "src/main/java/com/settings/MainActivity.kt",
    "src/main/java/com/ui/auth/ModernLoginScreen.kt", 
    "src/main/java/com/ui/auth/ModernSignupScreen.kt",
    "src/main/java/com/ui/auth/OctaAILoginScreen.kt"
]

for filepath in files_to_fix:
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # OctaApplication import'unu kaldır
        content = re.sub(r'import com\.aihackathonkarisacikartim\.god2\.OctaApplication\n?', '', content)
        
        # OctaApplication kullanımlarını değiştir
        content = re.sub(r'LocalContext\.current\.applicationContext as OctaApplication', 
                        'LocalContext.current', content)
        
        # application.sessionManager -> SessionManager(context)
        content = re.sub(r'application\.sessionManager', 'SessionManager(context)', content)
        
        # application.languageManager -> LanguageManager(context)  
        content = re.sub(r'application\.languageManager', 'LanguageManager(context)', content)
        
        # Gerekli import'ları ekle (eğer yoksa)
        if 'import com.settings.SessionManager' not in content:
            # Package satırından sonra ekle
            content = re.sub(r'(package [^\n]+\n)', r'\1import com.settings.SessionManager\n', content)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
            
        print(f"Fixed: {filepath}")
        
    except Exception as e:
        print(f"Error fixing {filepath}: {e}")