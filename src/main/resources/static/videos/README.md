# Wedding films

The supplied woodland wedding clip is stored at:

`weddings/woodland/woodland-wedding-ceremony.mp4`

This is the same woodland celebration as Photos 4–6. The original MOV contains 5.1 seconds of H.264 video at 512 × 910, with no audio track. It has been remuxed into MP4 without re-encoding, with fast-start enabled. The original attachment remains unchanged.

Its matching still is `images/weddings/woodland/woodland-wedding-ceremony-poster.jpg`, extracted two seconds into the clip. The original framing and visible creator credits are preserved.

The Weddings page automatically includes its styled film section when the file is present in the built application's resources. Without it, the whole section is omitted: no broken player or empty placeholder is shown.

The player uses native controls, plays inline on mobile, never autoplays and requests no video preload. The layout follows the clip's portrait proportions without cropping. The caption tells visitors that the clip has no sound.

This silent clip needs no speech captions. If a replacement includes speech, add accurate English WebVTT captions alongside it as `woodland-wedding-ceremony.en.vtt`; these are detected automatically.

After adding or replacing either file, restart local development with `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`) so Maven copies the resources. For production, rebuild and redeploy the application. This source folder is not a live production upload directory.

Check `/weddings#ceremony-film` after restarting. Verify sound, playback, seeking, captions and fullscreen using the actual film.

Use the same celebration folder and filename prefix for related photographs, film, poster and any future captions. Keep filenames descriptive and lower-case with hyphens. Record source-to-file mappings in `docs/ceremony-photography.md`.
