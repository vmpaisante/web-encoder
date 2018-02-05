package webencoder.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

/*
Interface for the storage service,

This interface declares functions necessary for a storage service.
Such service should be able to, from a MultipartFile file,
store the whole file in it's storage medium.
It should be able to load a file and return a file's path inside the
storage medium.
*/
public interface StorageService {

    void init();

    void store(MultipartFile file, String path);

    Resource load(String filename, String local_path);

    String path(String filename, String local_path);

}
