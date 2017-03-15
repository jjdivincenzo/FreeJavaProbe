///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
///////////////////////////////////////////////////////////////////////////////////
package edu.regis.jprobe.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * @author jdivince
 *
 */
public class ProbeInputStream extends ObjectInputStream {
    
    private long bytesRead = 0;
    private long bytesReadUncompressed = 0;
   
    public ProbeInputStream(InputStream in) throws IOException {
        super(in);
    }
   

    public Object readObject(boolean compressed) throws ClassNotFoundException, IOException {
        
        byte[] data = (byte[]) super.readObject();
       
        
        
        bytesRead += data.length;
        if (compressed) {
            
            data = Utilities.decompress(data);
            bytesReadUncompressed += data.length;
            
        } 
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        return ois.readObject();
    }


    public void close() throws IOException {
        super.close();
        //System.out.println("Bytes Read = " + bytesRead);
        //System.out.println("Bytes Read Uncompressed = " + bytesReadUncompressed);
        
    }
    /**
     * @return the bytesRead
     */
    public long getBytesRead() {
        return bytesRead;
    }
    /**
     * @return the bytesReadUncompressed
     */
    public long getBytesReadUncompressed() {
        return bytesReadUncompressed;
    }

}
