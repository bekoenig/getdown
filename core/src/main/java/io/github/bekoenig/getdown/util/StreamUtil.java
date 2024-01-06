//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import static io.github.bekoenig.getdown.Log.log;

public final class StreamUtil {
    /**
     * Convenient close for a stream. Use in a finally clause and love life.
     */
    public static void close (InputStream in)
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                log.warning("Error closing input stream", "stream", in, "cause", ioe);
            }
        }
    }

    /**
     * Convenient close for a stream. Use in a finally clause and love life.
     */
    public static void close (OutputStream out)
    {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ioe) {
                log.warning("Error closing output stream", "stream", out, "cause", ioe);
            }
        }
    }

    /**
     * Convenient close for a Reader. Use in a finally clause and love life.
     */
    public static void close (Reader in)
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                log.warning("Error closing reader", "reader", in, "cause", ioe);
            }
        }
    }

    /**
     * Convenient close for a Writer. Use in a finally clause and love life.
     */
    public static void close (Writer out)
    {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ioe) {
                log.warning("Error closing writer", "writer", out, "cause", ioe);
            }
        }
    }

    /**
     * Copies the contents of the supplied input stream to the supplied output stream.
     */
    public static <T extends OutputStream> T copy (InputStream in, T out)
        throws IOException
    {
        byte[] buffer = new byte[4096];
        for (int read = 0; (read = in.read(buffer)) > 0; ) {
            out.write(buffer, 0, read);
        }
        return out;
    }

}
