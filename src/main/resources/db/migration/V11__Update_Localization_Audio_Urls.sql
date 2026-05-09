-- Update main stalls to have the standard /audio/ prefix and vi suffix
UPDATE food_stalls SET audio_url = '/audio/' || id || '_vi.mp3';

-- Update all localizations to match the generated file pattern
UPDATE food_stall_localizations SET audio_url = '/audio/' || food_stall_id || '_' || language_code || '.mp3';
