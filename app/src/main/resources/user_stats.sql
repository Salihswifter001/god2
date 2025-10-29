CREATE TABLE IF NOT EXISTS public.user_stats (
  id SERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  created_musics INTEGER DEFAULT 0,
  favorite_genre TEXT,
  membership_type TEXT DEFAULT 'Standard',
  join_date DATE DEFAULT CURRENT_DATE,
  last_login_date DATE,
  UNIQUE(user_id)
);

-- Tabloyu yetkilendir
ALTER TABLE public.user_stats ENABLE ROW LEVEL SECURITY;

-- Herkesin kendi istatistiklerini görmesine izin veren RLS politikası
CREATE POLICY "Kullanıcılar kendi istatistiklerini görebilir" ON public.user_stats FOR SELECT
USING (auth.uid() = user_id);

-- Sadece kullanıcının kendi verilerini güncellemesine izin veren RLS politikası
CREATE POLICY "Kullanıcılar kendi istatistiklerini güncelleyebilir" ON public.user_stats FOR UPDATE
USING (auth.uid() = user_id);

-- Sadece kullanıcının kendi verilerini eklemesine izin veren RLS politikası
CREATE POLICY "Kullanıcılar kendileri için istatistik oluşturabilir" ON public.user_stats FOR INSERT
WITH CHECK (auth.uid() = user_id); 