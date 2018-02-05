package webencoder.encoding;

public interface EncodingService {

    void init();

    String encode(
        String input_filename,
        String input_path,
        String output_filename,
        String output_path
    );
}
