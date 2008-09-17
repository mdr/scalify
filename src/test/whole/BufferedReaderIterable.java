import java.io.*;
import java.util.*;

public class BufferedReaderIterable implements Iterable<String> {

    private BufferedReader mine;
    private Iterator<String> i;

    public BufferedReaderIterable( BufferedReader br ) {
        i = new BufferedReaderIterator( br );
    }

    public BufferedReaderIterable( File f ) throws FileNotFoundException {
        mine = new BufferedReader( new FileReader( f ) );
        i = new BufferedReaderIterator( mine );
    }
    public Iterator<String> iterator() {
        return i;
    }

    private class BufferedReaderIterator implements Iterator<String> {
        private BufferedReader br;
        private String line;

        public BufferedReaderIterator( BufferedReader aBR ) {
            (br = aBR).getClass();
            advance();
        }

        public boolean hasNext() {
            return line != null;
        }

        public String next() {
            String retval = line;
            advance();
            return retval;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported on BufferedReader iteration.");
        }

        private void advance() {
            try {
                line = br.readLine();
            }
            catch (IOException e) { /* TODO */}
            if ( line == null && mine != null ) {
                try {
                    mine.close();
                }
                catch (IOException e) { /* Ignore - probably should log an error */ }
                mine = null;
            }
        }
    }
}
