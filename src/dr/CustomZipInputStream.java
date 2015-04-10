package dr;


import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class CustomZipInputStream extends ZipInputStream {

    private boolean _canBeClosed = false;

    public CustomZipInputStream(InputStream is) {
        super(is);
    }

    @Override
    public void close() throws IOException {

        if(_canBeClosed) super.close();
    }

    public void allowToBeClosed() { _canBeClosed = true; }
}

