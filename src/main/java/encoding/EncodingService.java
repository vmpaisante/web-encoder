package webencoder.encoding;

/*
Interface for the encoding service,

This interface declares functions necessary for an encoding service.
Such service should be able to, from one file, create an encoded version
of it in a specified path with a defined name.
*/
public interface EncodingService {

    void init();

    String encode(
        String input_filename,
        String input_path,
        String output_filename,
        String output_path
    );
}
