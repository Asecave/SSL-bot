package main;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClipboardUtil {
    private static final Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static final ClipboardOwner owner = new Owner();
    
    
    public static void copy(BufferedImage img) {
        copy(new TransferableImage(img));
    }
    public static void copy(Transferable trans) {
        c.setContents(trans, owner);
    }
}
class Owner implements ClipboardOwner {

    @Override
    public void lostOwnership(Clipboard arg0, Transferable arg1) {
        System.out.println( "Lost Clipboard Ownership" );
    }
    
}
class TransferableImage implements Transferable {

    Image i;

    public TransferableImage( Image i ) {
        this.i = i;
    }

    public Object getTransferData( DataFlavor flavor )
    throws UnsupportedFlavorException, IOException {
        if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
            return i;
        }
        else {
            throw new UnsupportedFlavorException( flavor );
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = new DataFlavor[ 1 ];
        flavors[ 0 ] = DataFlavor.imageFlavor;
        return flavors;
    }

    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        DataFlavor[] flavors = getTransferDataFlavors();
        for ( int i = 0; i < flavors.length; i++ ) {
            if ( flavor.equals( flavors[ i ] ) ) {
                return true;
            }
        }

        return false;
    }
}