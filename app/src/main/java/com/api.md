val client = OkHttpClient()
val mediaType = "application/json".toMediaType()
val body = "{\n  \"prompt\": \"A calm and relaxing piano track with soft melodies\",\n  \"style\": \"Classical\",\n  \"title\": \"Peaceful Piano Meditation\",\n  \"customMode\": true,\n  \"instrumental\": true,\n  \"model\": \"V4\",\n  \"negativeTags\": \"\",\n  \"callBackUrl\": \"https://api.example.com/callback\"\n}".toRequestBody(mediaType)
val request = Request.Builder()
  .url("https://apibox.erweima.ai/api/v1/generate")
  .post(body)
  .addHeader("Content-Type", "application/json")
  .addHeader("Accept", "application/json")
  .addHeader("Authorization", "Bearer \${BuildConfig.MUSIC_API_KEY}") // API key BuildConfig'den alınıyor
  .build()
val response = client.newCall(request).execute()




// yönerge:
//prompt kısmı kullanıcının girdiği vokal inputu barındıracak
//style kısmı android uygulamadaki prompt inputunu barındıracak
//Negative Tags boş olacak
