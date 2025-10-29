package com.musicApi

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aihackathonkarisacikartim.god2.GeneratedMusicData
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.useCredit
import com.getUserStats
import com.incrementCreatedMusic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.os.Build
import java.io.File
import java.util.UUID
import kotlinx.coroutines.delay
import com.ui.SongItem
import com.settings.SessionManager

/**
 * UI durumunu temsil eden sealed class
 */
sealed class MusicUiState {
    object Idle : MusicUiState()
    data class Loading(val message: String = "Müzik oluşturuluyor...") : MusicUiState()
    data class Success(val message: String = "Müzik başarıyla oluşturuldu!") : MusicUiState()
    data class Error(val message: String = "Müzik oluşturulurken bir hata oluştu.") : MusicUiState()
}

/**
 * Müzik oluşturma ekranı için ViewModel
 */
class MusicViewModel(application: Application) : AndroidViewModel(application) {
    
    // API servisi
    private val musicApiService = MusicApiService(application.applicationContext)
    private val supabaseManager = SupabaseManager()
    private val sessionManager = SessionManager(application.applicationContext)
    
    // ViewModel başlatıldığında bekleyen müziği kontrol et
    init {
        // İlk açılışta bekleyen müzikleri kontrol et, ama state Loading değilse
        viewModelScope.launch {
            delay(100) // UI'nin yüklenmesini bekle
            if (_uiState.value !is MusicUiState.Loading) {
                checkPendingMusic()
            }
        }
        
        // Her 30 saniyede bir bekleyen müzikleri kontrol et (uygulama açıkken)
        viewModelScope.launch {
            while (true) {
                delay(30000) // 30 saniye bekle
                // Sadece Loading state'inde değilse kontrol et
                if (sessionManager.getPendingMusicTask() != null && _uiState.value !is MusicUiState.Loading) {
                    checkPendingMusic()
                }
            }
        }
    }
    
    // Detailed music creation stages - 60 stages for 3 minutes
    private val detailedMusicStages = listOf(
        // First 30 seconds - Analysis phase (0-16%)
        "Analyzing your musical DNA...",
        "Creating your harmony palette...",
        "Designing melodic structure...",
        "Determining tonal center...",
        "Scanning rhythm patterns...",
        "Calculating BPM and tempo...",
        "Applying music theory rules...",
        "Setting acoustic parameters...",
        "Optimizing frequency spectrum...",
        "Calculating dynamic range...",
        
        // 30-60 seconds - Basic structure (17-33%)
        "Weaving main melody line...",
        "Applying counterpoint techniques...",
        "Placing bass frequencies...",
        "Adding sub-bass layers...",
        "Programming kick drum pattern...",
        "Arranging snares and hi-hats...",
        "Placing percussion elements...",
        "Adding ghost notes...",
        "Adjusting swing and groove...",
        "Experimenting with polyrhythmic structures...",
        
        // 60-90 seconds - Instrumentation (34-50%)
        "Recording piano chords...",
        "Designing synthesizer leads...",
        "Adding analog warmth...",
        "Processing guitar harmonics...",
        "Arranging string section...",
        "Adding brass section...",
        "Placing woodwind nuances...",
        "Creating pad layers...",
        "Weaving ambient textures...",
        "Programming arpeggio patterns...",
        
        // 90-120 seconds - Detailing (51-66%)
        "Applying chorus effect...",
        "Adjusting reverb depth...",
        "Calculating delay timings...",
        "Setting compression threshold...",
        "Shaping EQ frequencies...",
        "Applying sidechain ducking...",
        "Adjusting stereo width...",
        "Adding panning automation...",
        "Programming filter sweeps...",
        "Placing pitch bends...",
        
        // 120-150 seconds - Advanced processing (67-83%)
        "Dosing harmonic distortion...",
        "Adding tape saturation...",
        "Experimenting with bit crusher effects...",
        "Applying granular synthesis...",
        "Processing vocoder...",
        "Adding ring modulation...",
        "Adjusting phaser sweeps...",
        "Setting flanger depth...",
        "Synchronizing tremolo rate...",
        "Creating auto-pan patterns...",
        
        // 150-180 seconds - Final stage (84-100%)
        "Applying multi-band compression...",
        "Setting limiter threshold...",
        "Optimizing LUFS value...",
        "Checking true peak...",
        "Selecting dithering algorithm...",
        "Performing sample rate conversion...",
        "Finalizing stereo imaging...",
        "Adding final harmonic enhancement...",
        "Finalizing master chain...",
        "Preparing your music..."
    )
    
    // Additional stages for vocal music
    private val vocalProcessingStages = listOf(
        "Analyzing vocal formants...",
        "Applying pitch correction...",
        "Layering vocal harmonies...",
        "Setting de-esser frequencies...",
        "Applying breath control...",
        "Customizing vocal reverb...",
        "Adding double tracking...",
        "Synchronizing vocal delays...",
        "Applying formant shifting...",
        "Enhancing vocal presence..."
    )
    
    // UI durumları
    private val _uiState = MutableStateFlow<MusicUiState>(MusicUiState.Idle)
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()
    
    // Progress için ayrı state
    private val _currentProgress = MutableStateFlow(0f)
    val currentProgress: StateFlow<Float> = _currentProgress.asStateFlow()
    
    // Kullanıcının girdiği metin, müzik türü ve seçilen dosya
    private val _prompt = mutableStateOf("")
    val prompt: State<String> = _prompt
    
    // Başlık için state değişkeni
    private val _title = mutableStateOf("")
    val title: State<String> = _title
    
    private val _selectedGenre = mutableStateOf("Pop")
    val selectedGenre: State<String> = _selectedGenre
    
    // Kredi bilgisi için state
    private val _remainingCredits = mutableStateOf(0)
    val remainingCredits: State<Int> = _remainingCredits
    
    // Vokal modu için state
    private val _isVocalModeActive = mutableStateOf(false)
    val isVocalModeActive: State<Boolean> = _isVocalModeActive
    
    private val _vocalInput = mutableStateOf("")
    val vocalInput: State<String> = _vocalInput
    
    private val _recordedVocalPath = mutableStateOf<String?>(null)
    val recordedVocalPath: State<String?> = _recordedVocalPath
    
    // Oluşturulan müzik ve kapak resmi için dosya yolları
    private var _musicFilePath = mutableStateOf<String?>(null)
    val musicFilePath = _musicFilePath
    
    private var _coverArtPath = mutableStateOf<String?>(null)
    val coverArtPath = _coverArtPath
    
    // Kullanıcı müzikleri
    private val _userMusics = mutableStateOf<List<GeneratedMusicData>>(emptyList())
    val userMusics: State<List<GeneratedMusicData>> = _userMusics
    
    // Müzik türlerine özel prompt listeleri (Her tür için 20 adet)
    private val genrePrompts: Map<String, List<String>> = mapOf(
        "Pop" to listOf(
            "Modern pop song with catchy melodies and upbeat rhythms",
            "Pop ballad with emotional vocals, soft and slow tempo",
            "Synth-heavy, 80s-inspired, energetic pop track",
            "Dance floor pop hit with strong choruses and electronic backing",
            "Tropical-influenced, summery and relaxing pop song",
            "Minimalist pop arrangement with acoustic guitar and piano",
            "R&B-influenced pop song with rhythmic and smooth vocals",
            "Dark and atmospheric pop track with slight experimental elements",
            "Powerful pop anthem with motivational and inspiring lyrics",
            "Cheerful and childlike pop song with children's choir or simple instrumentation",
            "Cinematic pop with a journey feel and wide instrumentation",
            "Cyberpunk pop with retro synths and futuristic soundscapes",
            "Whispered pop with minimalist rhythms and breathy vocals",
            "Avant-garde pop with sharp percussion and unexpected breaks",
            "Flamenco pop featuring Latin rhythms and Spanish guitar",
            "Ethnic pop with tribal rhythms and nature sounds",
            "Drum and bass-influenced pop with high-tempo drums and bass line",
            "Jazz pop featuring jazz chords and scat vocals",
            "Rock pop with heavy riffs and distorted guitars",
            "Symphonic pop with classical strings and choir"
        ),
        "Rock" to listOf(
            "Classic rock anthem with powerful guitar riffs and energetic drums",
            "Rock ballad with slow tempo and emotional guitar solo",
            "Grunge-influenced rock track with dirty guitars and intense vocals",
            "Punk rock with fast rhythms and rebellious vocals",
            "Alternative rock with atmospheric guitars and melancholic mood",
            "Heavy metal with brutal riffs and double bass drums",
            "Progressive rock with complex structure and variable tempos",
            "Blues rock with slide guitar and harmonica solos",
            "Southern rock with memorable riffs and dual guitar arrangements",
            "Indie rock with lo-fi production and melancholic vocals",
            "Protest rock with hard riffs and political lyrics",
            "Noise rock with noisy feedback and minimalist rhythms",
            "Folk rock with acoustic guitar and calm vocals",
            "Post rock with wide soundscapes and reverberant guitars",
            "Symphonic rock with orchestral arrangements and epic vocals",
            "Ska punk with ska rhythms and brass section",
            "Hardcore punk with fast tempos and screaming vocals",
            "Psychedelic rock with echoing guitars and dreamlike atmosphere",
            "Gothic rock with dark atmosphere and deep vocals",
            "Glam rock with shiny guitars and androgynous vocals"
        ),
        "Hip Hop" to listOf(
            "Classic hip hop with boom bap rhythms and scratch effects",
            "Jazzy hip hop with smooth bass lines and lyrical flow focus",
            "Modern trap hip hop with synth bass and trap drums",
            "Lo-fi hip hop with melancholic piano and sad beats",
            "R&B-influenced hip hop with soul samples and emotional vocals",
            "G-funk with slow tempo and funk guitars",
            "Gangsta rap with hard lyrics and urban soundscapes",
            "Hardcore hip hop with aggressive vocals and intense beats",
            "Conscious hip hop with meaningful lyrics and positive messages",
            "Comedy hip hop with humorous lyrics and fun beats",
            "Experimental hip hop with abstract beats and unusual vocal techniques",
            "Acid jazz hip hop with jazz flute and upright bass",
            "Reggae hip hop with reggae bass line and off-beat guitars",
            "Industrial hip hop with industrial sounds and mechanical rhythms",
            "Arabic hip hop with Middle Eastern melodies and rhythms",
            "Asian hip hop with Asian instruments and melodies",
            "Gospel hip hop with gospel choirs and organ",
            "Latin hip hop with Latin percussion and Spanish vocals",
            "Afro hip hop with Afrobeat rhythms and choir vocals",
            "Country hip hop with wild west atmosphere and banjo"
        ),
        "Elektronik" to listOf(
            "Techno with deep bass and minimal rhythms",
            "House music with energetic beats and catchy synth melodies",
            "Drum and bass with fast tempos and breakbeat drums",
            "Ambient electronic with atmospheric pads and broken rhythms",
            "Dubstep with distorted bass and aggressive synths",
            "Trance music with dreamlike pads and flowing rhythms",
            "Synthwave with retro synths and futuristic melodies",
            "Industrial electronic with industrial sounds and hard rhythms",
            "Minimal techno with minimalist rhythms and hypnotic loops",
            "Chiptune with chip music sounds and fast tempos",
            "J-Core with traditional Japanese instruments and electronic rhythms",
            "Dark ambient with dark ambient pads and disturbing soundscapes",
            "Psytrance with psychedelic soundscapes and fast tempos",
            "Dub electronic with reggae bass line and dub effects",
            "IDM (Intelligent Dance Music) with sharp percussion and polyrhythms",
            "Electronic pop with synth pop vocals and memorable melodies",
            "Chillwave with ocean sounds and slow tempos",
            "Ethnic electronic with Asian instruments and exotic soundscapes",
            "Symphonic electronic with classical orchestra instruments and electronic beats",
            "Noise electronic with noise generators and extreme distortion"
        ),
        "EDM" to listOf(
            "Festival EDM with energetic drops and high-tempo rhythms",
            "Future bass with emotional vocal chops and powerful bass drops",
            "Electro house style with hard synths and powerful drum hits",
            "Big room house with bright synth leads and rising build-ups",
            "Tropical house with summer themes, tropical beats and catchy melodies",
            "Deep house with deep bass hits and dark atmosphere",
            "Progressive house with cheerful melodies and vibrant rhythms",
            "Hardstyle with energetic kick drums and distorted synths",
            "Future house with groovy bass lines and chopped vocal samples",
            "Jersey club style fast rhythms and repeating vocal chops",
            "Melbourne bounce style bouncing rhythms and minimalist melodies",
            "Melodic dubstep with atmospheric melodies and emotional drops",
            "Moombahton with medium tempo rhythms and Latin-influenced percussion",
            "UK garage style shuffled beats and chopped vocals",
            "Bass house with growling basslines and techno-influenced percussion",
            "Trap EDM with 808 drums and electronic synth arpeggios",
            "Slap house with smooth bass lines and minimal top melodies",
            "Frenchcore with fast tempo and distorted kick drums",
            "Glitch hop with broken beats and digital sound manipulations",
            "Psytrance with hypnotic rhythms and hallucinogenic soundscapes"
        ),
         "Classical" to listOf(
            "Dramatic and emotional symphony written for a large orchestra",
            "Calm and peaceful piano sonata",
            "Lively and cheerful piece for string quartet",
            "Baroque style work featuring counterpoint and fugue",
            "Romantic period piece with passionate melodies and rich harmony",
            "Modern composition featuring atonality and unusual instrumentation",
            "Virtuoso concerto for solo violin",
            "Opera aria with powerful vocals and orchestra accompaniment",
            "Sacred music written for choir with large choir and organ accompaniment",
            "Chamber music, intimate and detailed piece for small ensemble",
            "Minimalist composition with repeating motifs and slow changes",
            "Film music style epic and exciting orchestral work",
            "Early period music with medieval hymns and Gregorian chants",
            "Renaissance dance music and madrigals",
            "Orchestra work influenced by classical Indian music",
            "Orchestra work featuring Japanese pentatonic scales and shakuhachi flute",
            "Orchestra work featuring Chinese instruments and melodies",
            "Orchestra work featuring Arabic maqams and oud",
            "Classical music incorporating jazz harmonies and rhythms",
            "Combination of electronic soundscapes and classical instruments"
        ),
        "Jazz" to listOf(
            "Smooth jazz with relaxing saxophone melodies and soft drums",
            "Upbeat swing with big orchestra and energetic rhythms",
            "Blues-influenced jazz with slide guitar and melancholy trumpet",
            "Modal jazz with minimalist structures and improvisation-focused",
            "Cool jazz with calm tempos and elegant melodies",
            "Hard bop with fast tempos and aggressive improvisations",
            "Fusion jazz with rock rhythms and electronic instruments",
            "Latin jazz with bossa nova or salsa rhythms and brass section",
            "Dixieland jazz with cheerful ensemble featuring banjo and tuba",
            "Avant-garde jazz with free improvisation and atonality",
            "Gypsy jazz with acoustic guitars and fast tempos",
            "Soul jazz with groovy rhythms and gospel influences",
            "Funk jazz with slap bass and sharp guitars",
            "Acid jazz with hip hop beats and jazz samples",
            "Big band jazz with wide orchestra arrangements and solo instruments",
            "Vocal jazz with scat vocals and smooth vocalists",
            "Piano trio jazz focused on piano, bass and drums",
            "Organ trio jazz focused on Hammond organ, guitar and drums",
            "Cuban jazz with Afro-Cuban rhythms and percussion",
            "Brazilian jazz with samba or bossa nova rhythms and acoustic guitars"
        ),
        "Ambient" to listOf(
            "Peaceful ambient with slowly changing pads and drones",
            "Atmospheric ambient with nature sounds and reverberant instruments",
            "Drone ambient with minimalist structures and repeating motifs",
            "Dark ambient with dark and disturbing soundscapes",
            "Industrial ambient with industrial sounds and mechanical rhythms",
            "Space ambient with wide and spacious sound spaces",
            "Underwater ambient with underwater sounds and muffled instruments",
            "Forest ambient with forest sounds and bird chirping",
            "Urban ambient with city noise and echoing sounds",
            "Dream pop ambient with dreamlike vocals and reverberant guitars",
            "Ethnic ambient combining traditional instruments and electronic sounds",
            "Classical ambient with classical piano and atmospheric pads",
            "Film music style epic and emotional ambient",
            "Sub bass ambient with low-frequency vibrations and deep resonances",
            "Lo-fi ambient with broken recordings and old tape sounds",
            "Reverberant ambient with intense reverb and delay effects",
            "Meditative ambient with long-lasting notes and slow changes",
            "Noise ambient with chaotic and irregular soundscapes",
            "Toytronica ambient with toy instruments and simple melodies",
            "Mysterious ambient with mysterious atmosphere and unknown sounds"
        ),
        "Lo-Fi" to listOf(
            "Relaxing lo-fi hip hop with vinyl crackle sounds and soft piano chords",
            "Lo-fi study beat with rain sounds and melancholy guitar melodies",
            "Vintage lo-fi with old radio sounds and muffled drums",
            "Calm and repeating lo-fi loops ideal for sleep",
            "Lo-fi jazz with jazz samples and hip hop rhythms",
            "Piano-focused, melancholic and calm lo-fi",
            "Guitar-focused, acoustic and relaxing lo-fi",
            "Synth-focused, dreamlike and atmospheric lo-fi",
            "Bass-focused, groovy and minimal lo-fi",
            "Drum-focused, breakbeat and hip hop lo-fi",
            "Vocal lo-fi with vocal samples and repeating melodies",
            "Film music influenced, cinematic lo-fi",
            "Video game music influenced, chiptune lo-fi",
            "Anime music influenced, J-pop lo-fi",
            "Lo-fi with nature sounds and animal sounds",
            "Lo-fi with city sounds and traffic sounds",
            "Lo-fi with cafe or library atmosphere",
            "Winter themed lo-fi with snow sounds and cold atmosphere",
            "Summer themed lo-fi with cricket sounds and warm atmosphere",
            "Night themed lo-fi with star sounds and quiet atmosphere"
        ),
        "Trap" to listOf(
            "Energetic trap beat with powerful 808 bass and hi-hats",
            "Emotional trap with melodic piano and auto-tuned vocals",
            "Dark trap with dark synths and eerie atmosphere",
            "Hype trap with minimalist beats and repetitive vocals",
            "Exotic trap featuring flute or ethnic instruments",
            "Rock-influenced trap with guitar riffs",
            "Jazzy trap with jazz chords and hip hop rhythms",
            "Chiptune trap with video game sounds and 8-bit melodies",
            "Asian trap with Asian instruments and pentatonic scales",
            "Latin trap with Latin percussion and Spanish vocals",
            "Afro trap with Afrobeat rhythms and choir vocals",
            "Country trap with country instruments and banjo",
            "Symphonic trap with classical strings and choir",
            "Slow tempo trap with ambient pads",
            "Industrial trap with noise effects and distortion",
            "Reggae trap with reggae bass line and off-beat rhythms",
            "Soul trap with soul samples and R&B vocals",
            "Rock trap with rock guitars and drums",
            "Metal trap with metal vocals and heavy riffs",
            "Blues trap with blues scales and slide guitar"
        ),
        "R&B" to listOf(
            "R&B ballad with smooth vocals and slow tempo",
            "Danceable R&B with funky bass line and groovy rhythms",
            "Modern R&B with modern synths and trap influences",
            "Neo-soul with lively instrumentation and complex harmonies",
            "R&B with hip hop beats and rap vocals",
            "Whispered R&B with minimalist rhythms and breathy vocals",
            "R&B with classic soul samples and vintage sound",
            "Minimalist R&B with acoustic guitar and piano accompaniment",
            "Jazzy R&B with jazz chords and improvisation instruments",
            "Latin R&B with Latin percussion and Spanish vocals",
            "Afro R&B with Afrobeat rhythms and choir vocals",
            "Country R&B with country instruments and banjo",
            "Symphonic R&B with classical strings and choir",
            "Slow tempo R&B with ambient pads",
            "Industrial R&B with noise effects and distortion",
            "Reggae R&B with reggae bass line and off-beat rhythms",
            "Rock R&B with rock guitars and drums",
            "Metal R&B with metal vocals and heavy riffs",
            "Blues R&B with blues scales and slide guitar",
            "Gospel R&B with gospel choirs and organ"
        ),
        "Synthwave" to listOf(
            "80s synthwave with retro synths, arpeggiators and drum machines",
            "Darksynth with dark and atmospheric, dystopian cityscape feel",
            "Dynamic synthwave with energetic bass lines and sharp drums",
            "Emotional synthwave with melancholic pads and sad melodies",
            "Video game music influenced synthwave with 8-bit or 16-bit sounds",
            "Film music style epic and cinematic synthwave",
            "Jazzy synthwave with jazz chords and improvisation instruments",
            "Metal synthwave with metal riffs and heavy drums",
            "Slow tempo synthwave with ambient pads",
            "Industrial synthwave with noise effects and distortion",
            "Reggae synthwave with reggae bass line and off-beat rhythms",
            "Soul synthwave with soul samples and R&B vocals",
            "Rock synthwave with rock guitars and drums",
            "Symphonic synthwave with classical strings and choir",
            "Latin synthwave with Latin percussion and Spanish vocals",
            "Asian synthwave with Asian instruments and pentatonic scales",
            "Afro synthwave with Afrobeat rhythms and choir vocals",
            "Country synthwave with country instruments and banjo",
            "Blues synthwave with blues scales and slide guitar",
            "Gospel synthwave with gospel choirs and organ"
        ),
        "Techno" to listOf(
            "Hypnotic techno with deep bass and minimal rhythms",
            "Dance floor techno with energetic beats and repeating melodies",
            "Industrial techno with industrial sounds and hard drums",
            "Acid house influenced techno with 303 bass line",
            "Melodic techno with melodic pads and dreamlike atmosphere",
            "Minimal techno with minimalist structures and focused sounds",
            "Schranz with fast tempos and distorted percussion",
            "Detroit techno with soulful melodies and futuristic sounds",
            "Gabber with very fast tempos and hard bass drums",
            "Hardcore techno with aggressive sounds and intense atmosphere",
            "Dub techno with echoing drums and dub effects",
            "Ambient techno with slow tempos and atmospheric pads",
            "Noise techno with noisy soundscapes and chaotic rhythms",
            "Psychedelic techno with psychedelic soundscapes and repeating rhythms",
            "Tribal techno with tribal rhythms and ethnic percussion",
            "Acid ambient techno with 303 bass line and atmospheric pads",
            "Industrial ambient techno with industrial sounds and atmospheric pads",
            "Dark techno with dark atmosphere and disturbing sounds",
            "Future techno with futuristic soundscapes and experimental rhythms",
            "Sci-fi techno with science fiction themed sounds and atmospheres"
        ),
        "Country" to listOf(
            "Cheerful country song with acoustic guitar and banjo accompaniment",
            "Emotional country ballad with steel guitar solo",
            "Bluegrass-influenced country with fast fiddle melodies and energetic rhythms",
            "Outlaw country with harsh vocals and rebellious lyrics",
            "Modern country with pop influences and catchy choruses",
            "Texas country with honky-tonk rhythms and bar atmosphere",
            "Americana with folk, country and blues influences",
            "Western swing with big orchestra and swing rhythms",
            "Red Dirt country with rock influences and energetic style",
            "Bluegrass with fast tempos and virtuoso instrumentation",
            "Honky-tonk dance music with piano and slide guitar accompaniment",
            "Nashville sound with smooth vocals and orchestra accompaniment",
            "Cajun music, energetic dance music with accordion and violin",
            "Zydeco, Louisiana music with accordion and washboard",
            "Cowboy music with slow tempo and storytelling songs",
            "Gospel country with choir vocals and religious themes",
            "Outlaw bluegrass with fast tempos and rebellious lyrics",
            "Western blues with slide guitar and harmonica solos",
            "Country rock with rock rhythms and country instruments",
            "Country pop with pop arrangements and country vocals"
        ),
        "Metal" to listOf(
            "Classic heavy metal with heavy guitar riffs and powerful drums",
            "Death metal with double bass drums and brutal vocals",
            "Black metal with fast tempos and tremolo picking",
            "Technical death metal with technical riffs and variable rhythms",
            "Melodic death metal with melodic riffs and epic atmosphere",
            "Progressive metal with progressive structures and virtuoso instrumentation",
            "Metalcore with breakdowns and clean/scream vocals",
            "Djent with syncopated riffs and low-tuned guitars",
            "Doom metal with slow tempos and heavy riffs",
            "Gothic metal with female vocals and atmospheric keyboards",
            "Power metal with high tempos and epic vocals",
            "Thrash metal with fast tempos and aggressive riffs",
            "Glam metal with catchy choruses and guitar solos",
            "Nu metal with hip hop influences and low-tuned guitars",
            "Industrial metal with industrial sounds and mechanical rhythms",
            "Folk metal with folk music instruments and melodies",
            "Viking metal with epic themes and choir vocals",
            "Symphonic metal with orchestra and choir accompaniment",
            "Sludge metal with slow tempos and dirty riffs",
            "Stoner metal with groovy riffs and psychedelic effects"
        ),
        "Reggae" to listOf(
            "Roots reggae with off-beat guitar rhythms and calm bass line",
            "Dancehall with upbeat rhythms and danceable melodies",
            "Dub reggae with minimalist dub effects and echoing drums",
            "Lover's rock with calm vocals and acoustic instruments",
            "Jazz-influenced reggae with improvisation",
            "Reggae with hip hop beats and rap vocals",
            "Soul reggae with soul vocals and funky bass lines",
            "Rocksteady with slow tempos and smooth vocals",
            "Ska with fast tempos and brass section",
            "Dub poetry with rhythmic lyrics and musical accompaniment",
            "Reggaeton with reggaeton rhythms and Spanish vocals",
            "Slow tempo reggae with ambient pads",
            "Industrial reggae with noise effects and distortion",
            "Synthwave-influenced reggae with retro sounds",
            "Symphonic reggae with classical strings and choir",
            "Latin reggae with Latin percussion and Spanish vocals",
            "Asian reggae with Asian instruments and pentatonic scales",
            "Afro reggae with Afrobeat rhythms and choir vocals",
            "Country reggae with country instruments and banjo",
            "Blues reggae with blues scales and slide guitar"
        ),
        "Blues" to listOf(
            "Melancholy delta blues song with acoustic guitar and harmonica accompaniment",
            "Energetic Chicago blues song with electric guitar and bass accompaniment",
            "Jump blues with piano and wind instruments",
            "Slow tempo blues ballad with slide guitar solo",
            "Boogie-woogie piano rhythms",
            "Texas blues with clean guitar tones and long solos",
            "West Coast blues with jazz influences and swing rhythms",
            "Piedmont blues with fingerpicking guitar technique",
            "Electric blues with distorted guitars and powerful vocals",
            "Acoustic blues with only vocals and acoustic guitar",
            "Soul blues with soul vocals and R&B influences",
            "Blues rock with rock rhythms and blues scales",
            "Country blues with country instruments and themes",
            "Swamp blues with slow tempos and dirty sounds",
            "British blues with English rock influences",
            "Louisiana blues with zydeco and Cajun influences",
            "Hill country blues with repetitive rhythms and trance-like structure",
            "Piano blues focused on piano",
            "Guitar blues focused on guitar",
            "Harmonica blues focused on harmonica"
        )
        // Diğer türler için promptları buraya ekleyebilirsiniz.
    )
    
    init {
        // Kullanıcı oturum açmışsa, müziklerini getir
        viewModelScope.launch {
            loadUserMusics()
            loadUserCredits()
        }
    }
    
    /**
     * Seçilen müzik türüne göre rastgele bir promptu ayarlar.
     */
    fun setRandomPromptForGenre(genre: String) {
        val prompts = genrePrompts[genre]
        if (prompts != null && prompts.isNotEmpty()) {
            val randomPrompt = prompts.random()
            _prompt.value = randomPrompt
        }
    }
    
    /**
     * Prompt güncellemesi
     */
    fun updatePrompt(newPrompt: String) {
        _prompt.value = newPrompt
    }
    
    /**
     * Başlık güncellemesi
     */
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }
    
    /**
     * Seçilen müzik türünü güncelle
     */
    fun updateSelectedGenre(genre: String) {
        _selectedGenre.value = genre
    }
    
    /**
     * Vokal modunu aç/kapat
     */
    fun toggleVocalMode() {
        _isVocalModeActive.value = !_isVocalModeActive.value
        Log.d("MusicViewModel", "Vokal modu değişti: ${_isVocalModeActive.value}")
    }
    
    /**
     * Vokal input metnini güncelle
     */
    fun updateVocalInput(text: String) {
        _vocalInput.value = text
        Log.d("MusicViewModel", "Vokal input güncellendi: ${_vocalInput.value}")
    }
    
    /**
     * Şarkı sözlerini ayarla (vokal modu için)
     */
    fun setVocalLyrics(lyrics: String) {
        _vocalInput.value = lyrics
    }
    
    /**
     * Kaydedilen vokal dosya yolunu ayarla
     */
    fun setRecordedVocalPath(filePath: String?) {
        _recordedVocalPath.value = filePath
    }
    
    /**
     * Müzik oluşturma işlemini başlat
     */
    fun generateMusic() {
        if (prompt.value.isBlank()) {
            _uiState.value = MusicUiState.Error("Lütfen bir metin girin.")
            return
        }
        
        viewModelScope.launch {
            try {
                // Kullanıcı kontrolü
                val currentUser = supabaseManager.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = MusicUiState.Error("Lütfen önce giriş yapın.")
                    return@launch
                }
                
                // Kredi kontrolü
                val creditResult = supabaseManager.useCredit(currentUser.id)
                if (creditResult.isFailure) {
                    _uiState.value = MusicUiState.Error(creditResult.exceptionOrNull()?.message ?: "Her müzik oluşturma 10 kredi gerektirir. Yeterli krediniz bulunmamaktadır.")
                    return@launch
                }
                
                // Kredi bilgisini güncelle
                loadUserCredits()
                
                // Direkt ilk aşama mesajıyla başla
                _uiState.value = MusicUiState.Loading(detailedMusicStages.first())
                _currentProgress.value = 0f
                
                // 3 dakikalık progress simulasyonu başlat
                startProgressSimulation()
                
                // Vokal modu aktif mi kontrol et
                if (isVocalModeActive.value) {
                    if (vocalInput.value.isNotBlank()) {
                        // Şarkı sözleri metni kullanılacak
                        val result = musicApiService.generateMusicWithLyrics(prompt.value, selectedGenre.value, vocalInput.value, title.value)
                        if (result.isSuccess) {
                            // Task ID'yi al ve beklemek için durumu güncelle
                            val taskId = result.getOrNull()
                            
                            // TaskID'yi SessionManager'a kaydet
                            taskId?.let { id ->
                                sessionManager.savePendingMusicTask(id, title.value, prompt.value)
                                Log.d("MusicViewModel", "TaskID kaydedildi: $id")
                                
                                // Foreground Service başlat - ASLA DURMAZ!
                                startMusicGenerationService(id, title.value, prompt.value)
                            }
                            
                            // Progress simulasyonu devam etsin, mesaj gösterme
                            // Foreground Service zaten çalışıyor, kayıt işlemi Service'de yapılacak
                        } else {
                            _uiState.value = MusicUiState.Error("Hata: ${result.exceptionOrNull()?.message}")
                        }
                    } else if (recordedVocalPath.value != null) {
                        // Ses kaydı kullanma şimdilik desteklenmiyor
                        _uiState.value = MusicUiState.Error("Ses kaydı şu anda desteklenmiyor")
                    } else {
                        _uiState.value = MusicUiState.Error("Şarkı sözleri veya ses kaydı gerekiyor")
                    }
                } else {
                    // Enstrümantal müzik oluşturma
                    val result = musicApiService.generateMusic(prompt.value, selectedGenre.value, title.value)
                    
                    if (result.isSuccess) {
                        // Task ID'yi al ve beklemek için durumu güncelle
                        val taskId = result.getOrNull()
                        
                        // TaskID'yi SessionManager'a kaydet
                        taskId?.let { id ->
                            sessionManager.savePendingMusicTask(id, title.value, prompt.value)
                            Log.d("MusicViewModel", "TaskID kaydedildi (enstrümantal): $id")
                            
                            // Foreground Service başlat - ASLA DURMAZ!
                            startMusicGenerationService(id, title.value, prompt.value)
                        }
                        
                        // Progress simulasyonu devam etsin, mesaj gösterme
                        // Foreground Service zaten çalışıyor, kayıt işlemi Service'de yapılacak
                    } else {
                        _uiState.value = MusicUiState.Error("Hata: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MusicUiState.Error("Hata: ${e.message}")
            }
        }
    }
    
    /**
     * API bağlantısını test et
     */
    fun testApiConnection() {
        viewModelScope.launch {
            try {
                _uiState.value = MusicUiState.Loading("API bağlantısı test ediliyor...")
                // API bağlantı testi burada yapılacak
                // Bu bir mock test
                _uiState.value = MusicUiState.Success("API bağlantı testi başarılı!")
            } catch (e: Exception) {
                _uiState.value = MusicUiState.Error("API bağlantı testi başarısız: ${e.message}")
            }
        }
    }
    
    /**
     * Kullanıcı tarafından çağrılabilir public fonksiyon
     */
    fun checkForPendingMusic() {
        checkPendingMusic()
    }
    
    /**
     * Foreground Service başlat - Android tarafından ASLA durdurulmaz!
     */
    private fun startMusicGenerationService(taskId: String, title: String, prompt: String) {
        val context = getApplication<Application>()
        
        // Kullanıcı ID'sini al
        val currentUser = supabaseManager.getCurrentUser()
        val userId = currentUser?.id ?: return // User ID yoksa service başlatma
        
        val intent = Intent(context, MusicGenerationService::class.java).apply {
            action = MusicGenerationService.ACTION_START
            putExtra(MusicGenerationService.EXTRA_TASK_ID, taskId)
            putExtra(MusicGenerationService.EXTRA_TITLE, title)
            putExtra(MusicGenerationService.EXTRA_PROMPT, prompt)
            putExtra(MusicGenerationService.EXTRA_USER_ID, userId)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        
        Log.d("MusicViewModel", "Foreground Service başlatıldı: $taskId")
    }
    
    /**
     * Bekleyen müzik üretimini kontrol et
     */
    private fun checkPendingMusic() {
        viewModelScope.launch {
            try {
                val pendingTaskId = sessionManager.getPendingMusicTask()
                if (pendingTaskId != null) {
                    val pendingTitle = sessionManager.getPendingMusicTitle() ?: "Bekleyen Müzik"
                    val pendingPrompt = sessionManager.getPendingMusicPrompt() ?: ""
                    
                    Log.d("MusicViewModel", "Bekleyen müzik bulundu: TaskID=$pendingTaskId")
                    
                    // Auth durumunu kontrol et ve yenile (arkaplandan dönüş için)
                    try {
                        val currentUser = supabaseManager.getCurrentUser()
                        if (currentUser != null) {
                            Log.d("MusicViewModel", "Auth session aktif: ${currentUser.email}")
                        } else {
                            Log.w("MusicViewModel", "Auth session expire olmuş, giriş gerekli")
                        }
                    } catch (e: Exception) {
                        Log.e("MusicViewModel", "Auth kontrol hatası: ${e.message}")
                    }
                    
                    _uiState.value = MusicUiState.Loading("Tamamlanmamış müzik üretimi kontrol ediliyor...\n$pendingTitle")
                    
                    // API'ye durum kontrolü yap
                    val statusResult = musicApiService.checkMusicGenerationStatus(pendingTaskId)
                    
                    if (statusResult.isSuccess) {
                        val musicData = statusResult.getOrNull()
                        if (musicData != null) {
                            // Müzik hazır!
                            Log.d("MusicViewModel", "Bekleyen müzik hazır: $musicData")
                            
                            // Auth kontrolü yap
                            val currentUser = supabaseManager.getCurrentUser()
                            if (currentUser == null) {
                                Log.w("MusicViewModel", "Auth session yok, müzik kaydedilemiyor. Giriş yapılması gerekiyor.")
                                _uiState.value = MusicUiState.Error("Müziğiniz hazır! Kütüphaneye eklemek için lütfen tekrar giriş yapın.")
                                // Müzik bilgilerini sakla, kullanıcı giriş yaptığında eklenecek
                                return@launch
                            }
                            
                            // Önce bu müziğin zaten kaydedilip kaydedilmediğini kontrol et
                            val musicAlreadyExists = supabaseManager.checkMusicExistsByMusicId(
                                musicData.musicId,
                                currentUser.id
                            )
                            
                            if (musicAlreadyExists) {
                                Log.d("MusicViewModel", "Müzik zaten kaydedilmiş, tekrar eklenmeyecek: ${musicData.musicId}")
                                _uiState.value = MusicUiState.Success("Müziğiniz zaten kütüphanenizde mevcut!\n$pendingTitle")
                                sessionManager.clearPendingMusicTask()
                                
                                // Müzikleri yeniden yükle
                                loadUserMusics()
                            } else {
                                // Müziği Supabase'e kaydet
                                val saveResult = supabaseManager.saveGeneratedMusic(musicData)
                                if (saveResult.isSuccess) {
                                    Log.d("MusicViewModel", "Bekleyen müzik Supabase'e kaydedildi")
                                    
                                    // Oluşturulan müzik sayısını artır
                                    supabaseManager.incrementCreatedMusic(currentUser.id)
                                    
                                    _uiState.value = MusicUiState.Success("Bekleyen müziğiniz hazır ve kütüphanenize eklendi!\n$pendingTitle")
                                    sessionManager.clearPendingMusicTask()
                                    
                                    // Müzikleri yeniden yükle
                                    loadUserMusics()
                                } else {
                                    Log.e("MusicViewModel", "Bekleyen müzik Supabase'e kaydedilemedi: ${saveResult.exceptionOrNull()?.message}")
                                    _uiState.value = MusicUiState.Error("Müzik hazır ama kaydedilemedi. Lütfen tekrar deneyin.")
                                }
                            }
                        } else {
                            // Hala üretiliyor, mevcut state'i koru (Loading state'inde kalması için)
                            // State'i değiştirme, progress devam ediyor
                            Log.d("MusicViewModel", "Müzik hala üretiliyor: $pendingTaskId")
                        }
                    } else {
                        // Hata olmuş veya süre dolmuş
                        sessionManager.clearPendingMusicTask()
                        Log.e("MusicViewModel", "Bekleyen müzik kontrolü başarısız: ${statusResult.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Bekleyen müzik kontrolü hatası: ${e.message}")
            }
        }
    }
    
    /**
     * Kullanıcının tüm müziklerini yükler
     */
    private suspend fun loadUserMusics() {
        try {
            // Gerçek kullanıcı ID'sini al veya UUID oluştur
            val currentUser = supabaseManager.getCurrentUser()
            val userId = if (currentUser != null) {
                currentUser.id
            } else {
                // Mock UUID formatında ID
                UUID.randomUUID().toString()
            }
            
            val musics = try {
                supabaseManager.getUserGeneratedMusic(userId)
            } catch (e: Exception) {
                // Hata durumunda boş liste döndür
                Log.e("MusicViewModel", "Müzikleri getirme hatası: ${e.message}")
                emptyList()
            }
            _userMusics.value = musics
        } catch (e: Exception) {
            Log.e("MusicViewModel", "Müzikleri yükleme hatası: ${e.message}")
        }
    }
    
    /**
     * Kullanıcının kredi bilgisini yükler
     */
    private suspend fun loadUserCredits() {
        try {
            val currentUser = supabaseManager.getCurrentUser()
            if (currentUser != null) {
                val stats = supabaseManager.getUserStats(currentUser.id)
                _remainingCredits.value = stats.credits
            }
        } catch (e: Exception) {
            Log.e("MusicViewModel", "Kredi bilgisi yükleme hatası: ${e.message}")
        }
    }
    
    /**
     * 3 dakikalık progress simulasyonu
     */
    private fun startProgressSimulation() {
        viewModelScope.launch {
            val totalDuration = 180_000L // 3 dakika = 180 saniye = 180000 ms
            val updateInterval = 3000L // Her 3 saniyede bir güncelle
            val totalSteps = (totalDuration / updateInterval).toInt() // 60 adım
            
            // Vokal modunda ek mesajlar ekle
            val allStages = if (isVocalModeActive.value) {
                // Vokal mesajlarını rastgele pozisyonlara ekle
                val combinedList = detailedMusicStages.toMutableList()
                vocalProcessingStages.forEach { vocalStage ->
                    val randomIndex = (10..50).random() // Ortaya doğru ekle
                    combinedList.add(randomIndex, vocalStage)
                }
                combinedList
            } else {
                detailedMusicStages
            }
            
            var usedMessages = mutableSetOf<String>()
            
            for (step in 0..totalSteps) {
                // Eğer UI State Success olursa progress'i hızla tamamla
                if (_uiState.value is MusicUiState.Success) {
                    Log.d("MusicViewModel", "Müzik erken tamamlandı! Progress hızla tamamlanıyor...")
                    
                    // Progress'i hızla %100'e tamamla, mesajları da güncelle
                    val currentProgress = _currentProgress.value
                    val finalStages = listOf(
                        "Finalizing stereo imaging...",
                        "Applying final mastering...",
                        "Optimizing audio quality...",
                        "Completing music generation..."
                    )
                    var stageIndex = 0
                    
                    for (quickStep in currentProgress.toInt()..100 step 5) {
                        _currentProgress.value = quickStep.toFloat()
                        // Her 20 progresste mesajı değiştir
                        if (quickStep % 20 == 0 && stageIndex < finalStages.size) {
                            _uiState.value = MusicUiState.Loading(finalStages[stageIndex])
                            stageIndex++
                        } else if (_uiState.value !is MusicUiState.Loading) {
                            // Loading state'inde değilse tekrar Loading yap
                            _uiState.value = MusicUiState.Loading(if (stageIndex < finalStages.size) finalStages[stageIndex] else "Completing...")
                        }
                        delay(50) // Hızlı animasyon
                    }
                    _currentProgress.value = 100f
                    
                    // Update success message
                    _uiState.value = MusicUiState.Success("Your music has been successfully created!")
                    break
                }
                
                // Error durumunda direkt dur
                if (_uiState.value is MusicUiState.Error) {
                    Log.d("MusicViewModel", "Progress simulasyonu durduruldu - Hata oluştu")
                    break
                }
                
                val progress = (step.toFloat() / totalSteps) * 100f
                _currentProgress.value = progress
                
                // Rastgele ama daha önce kullanılmamış bir mesaj seç
                val availableMessages = allStages.filter { it !in usedMessages }
                val message = if (availableMessages.isNotEmpty()) {
                    availableMessages.random().also { usedMessages.add(it) }
                } else {
                    // Show fallback messages when all messages are used
                    when {
                        progress < 50 -> "Processing music elements..."
                        progress < 80 -> "Applying final touches..."
                        progress < 95 -> "Almost ready..."
                        else -> "Preparing your music..."
                    }
                }
                
                // Loading state'inde ise mesajı güncelle - her zaman güncelle
                if (_uiState.value is MusicUiState.Loading) {
                    _uiState.value = MusicUiState.Loading(message)
                } else if (step == 0) {
                    // İlk adımda Loading state'ine geç
                    _uiState.value = MusicUiState.Loading(message)
                }
                
                // Progress %100'e ulaştıysa bitir
                if (progress >= 100f) {
                    _currentProgress.value = 100f
                    break
                }
                
                delay(updateInterval)
            }
        }
    }
    
    /**
     * Kullanıcının müziklerini UI'da kullanılabilecek şekilde dönüştürür
     */
    suspend fun getUserMusicsAsSongItems(): List<SongItem> {
        if (_userMusics.value.isEmpty()) {
            loadUserMusics()
        }
        
        return _userMusics.value.map { music ->
            SongItem(
                id = music.id,
                title = music.title,
                artist = "OctaAI", // Sabit değer veya müziğin oluşturulma detaylarından alınabilir
                albumArt = supabaseManager.getCoverArtUrl(music.coverUrl),
                mediaUri = supabaseManager.getMusicFileUrl(music.musicUrl),
                duration = formatDuration(music.duration),
                genre = music.genre,
                promptText = music.prompt,
                createdAt = music.createdAt,
                durationInSeconds = music.duration.toInt(),
                coverArtUrl = music.coverUrl,
                musicId = music.musicId // API'den gelen müzik ID'si eklendi
            )
        }
    }
    
    /**
     * Saniye cinsinden süreyi formatlar
     */
    private fun formatDuration(durationSeconds: Long): String {
        if (durationSeconds <= 0) return "0:00"
        
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        
        return String.format("%d:%02d", minutes, seconds)
    }
    
    /**
     * Bir müziği siler
     */
    fun deleteMusic(musicId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = MusicUiState.Loading("Müzik siliniyor...")
                
                // Gerçek kullanıcı ID'sini al
                val currentUser = supabaseManager.getCurrentUser()
                val userId = currentUser?.id ?: return@launch
                
                try {
                    val result = supabaseManager.deleteUserMusic(userId, musicId)
                    
                    if (result.isSuccess) {
                        _uiState.value = MusicUiState.Success("Müzik başarıyla silindi")
                        loadUserMusics() // Müzikleri yeniden yükle
                    } else {
                        _uiState.value = MusicUiState.Error("Müzik silme hatası: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    _uiState.value = MusicUiState.Error("Müzik silme hatası: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.value = MusicUiState.Error("Müzik silme hatası: ${e.message}")
            }
        }
    }
} 