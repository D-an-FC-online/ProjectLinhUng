package vn.edu.sgu.damsen.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import vn.edu.sgu.damsen.config.AppProperties;

/** Đọc/ghi file âm thanh trong thư mục media (app.media-dir). storage_key = tên file. */
@Component
public class AudioStorage {
    private static final Logger log = LoggerFactory.getLogger(AudioStorage.class);
    private static final Map<String, MediaType> TYPES = Map.of(
            "mp3", MediaType.parseMediaType("audio/mpeg"),
            "m4a", MediaType.parseMediaType("audio/mp4"),
            "ogg", MediaType.parseMediaType("audio/ogg"),
            "wav", MediaType.parseMediaType("audio/wav"));

    private final Path root;

    public AudioStorage(AppProperties props) {
        this.root = chooseRoot(props.mediaDir());
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Không tạo được thư mục media " + root, e);
        }
        log.info("Thư mục audio: {}", root);
    }

    /** Chạy trong thư mục backend thì ../data/audio đúng; chạy từ thư mục gốc (IntelliJ) thì là data/audio. */
    private static Path chooseRoot(String configured) {
        Path p = Path.of(configured);
        if (p.isAbsolute() || Files.isDirectory(p)) return p.toAbsolutePath().normalize();
        for (String alt : new String[] {"data/audio", "../data/audio"}) {
            if (Files.isDirectory(Path.of(alt))) return Path.of(alt).toAbsolutePath().normalize();
        }
        return p.toAbsolutePath().normalize();
    }

    public static boolean isSupportedExtension(String ext) {
        return ext != null && TYPES.containsKey(ext.toLowerCase(Locale.ROOT));
    }

    public static String extensionOf(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return i < 0 ? "" : filename.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    public MediaType mediaType(String storageKey) {
        return TYPES.getOrDefault(extensionOf(storageKey), MediaType.APPLICATION_OCTET_STREAM);
    }

    public void write(String storageKey, byte[] data) {
        try {
            Files.write(resolve(storageKey), data, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("Không ghi được file " + storageKey, e);
        }
    }

    public Resource read(String storageKey) {
        Path p = resolve(storageKey);
        if (!Files.isRegularFile(p)) {
            throw ApiException.notFound("file âm thanh '" + storageKey + "' trong thư mục media");
        }
        return new FileSystemResource(p);
    }

    public boolean exists(String storageKey) {
        return Files.isRegularFile(resolve(storageKey));
    }

    /** Chặn đường dẫn kiểu "../" thoát ra ngoài thư mục media. */
    private Path resolve(String storageKey) {
        Path p = root.resolve(storageKey).normalize();
        if (!p.startsWith(root)) throw ApiException.badRequest("storage_key không hợp lệ");
        return p;
    }
}
