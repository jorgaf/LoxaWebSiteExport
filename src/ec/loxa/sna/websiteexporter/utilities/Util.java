/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java 
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */
package ec.loxa.sna.websiteexporter.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
/**
 * Util -- print or unzip a JAR or PKZIP file using java.util.zip. Command-line
 * version: extracts files.
 * 
 * @author Ian Darwin, Ian@DarwinSys.com $Id: Util.java,v 1.7 2004/03/07
 *         17:40:35 ian Exp $
 */
public class Util {

    /** Constants for mode listing or mode extracting. */
    public static final int LIST = 0, EXTRACT = 1;
    /** Whether we are extracting or just printing TOC */
    protected int mode = LIST;
    /** The ZipFile that is used to read an archive */
    protected ZipFile zippy;
    /** The buffer for reading/writing the ZipFile data */
    protected byte[] b;
    private File directoryToExtract;

    /** Construct an Util object. Just allocate the buffer */
    public Util() {
        b = new byte[8092];
    }

    /** Set the Mode (list, extract). */
    public void setMode(int m) {
        if (m == LIST || m == EXTRACT) {
            mode = m;
        }
    }
    /** Cache of paths we've mkdir()ed. */
    public SortedSet dirsMade;

    /** For a given Zip file, process each entry. */
    public void unZip(String zipPath) throws Exception {

        dirsMade = new TreeSet();
        try {
            zippy = new ZipFile(zipPath);
            Enumeration all = zippy.entries();
            while (all.hasMoreElements()) {
                getFile((ZipEntry) all.nextElement());
            }
            zippy.close();
        } catch (IOException err) {
            throw new Exception(err.getMessage());
        }
    }
    protected boolean warnedMkDir = false;

    /**
     * Process one file from the zip, given its name. Either print the name, or
     * create the file on disk.
     */
    private void getFile(ZipEntry e) throws IOException {
        String zipName = e.getName();
        switch (mode) {
            case EXTRACT:
                if (zipName.startsWith("/")) {
                    if (!warnedMkDir) {
                        Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, new Exception("Ignoring absolute paths"));
                    }
                    warnedMkDir = true;
                    zipName = zipName.substring(1);
                }
                // if a directory, just return. We mkdir for every file,
                // since some widely-used Zip creators don't put out
                // any directory entries, or put them in the wrong place.
                if (zipName.endsWith("/")) {
                    return;
                }
                // Else must be a file; open the file for output
                // Get the directory part.
                int ix = zipName.lastIndexOf('/');
                if (ix > 0) {
                    String dirName = zipName.substring(0, ix);
                    if (!dirsMade.contains(dirName)) {
                        File d = new File(getDirectoryToExtract() + (getDirectoryToExtract().getPath().endsWith(File.separator) ? "" : File.separator) + dirName);
                        // If it already exists as a dir, don't do anything
                        if (!(d.exists() && d.isDirectory())) {
                            // Try to create the directory, warn if it fails
                            //System.out.println("Creating Directory: " + dirName);
                            if (!d.mkdirs()) {
                                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, new Exception("Directory no created"));
                            }
                            dirsMade.add(dirName);
                        }
                    }
                }
                //System.err.println("Creating " + zipName);
                File file = new File(getDirectoryToExtract() + (getDirectoryToExtract().getPath().endsWith(File.separator) ? "" : File.separator) + zipName);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdir();
                }
                //zipName = "/Users/jorgaf/testWebSiteExporter/Project0/" + zipName;
                FileOutputStream os = new FileOutputStream(file);
                InputStream is = zippy.getInputStream(e);
                int n;
                while ((n = is.read(b)) > 0) {
                    os.write(b, 0, n);
                }
                is.close();
                os.close();
                break;
            case LIST:
                // Not extracting, just list
                if (e.isDirectory()) {
                    System.out.println("Directory " + zipName);
                } else {
                    System.out.println("File " + zipName);
                }
                break;
            default:
                throw new IllegalStateException("mode value (" + mode + ") bad");
        }
    }

    public void copyFromJar(String jar, String source) throws Exception {
        InputStream is = getClass().getResourceAsStream(jar + source);

        File file = new File(getDirectoryToExtract() + (getDirectoryToExtract().getPath().endsWith(File.separator) ? "" : File.separator) + source);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        os.close();
        is.close();
    }

    /**
     * @return the directoryToExtract
     */
    public File getDirectoryToExtract() {
        return directoryToExtract;
    }

    /**
     * @param directoryToExtract the directoryToExtract to set
     */
    public void setDirectoryToExtract(File directoryToExtract) {
        this.directoryToExtract = directoryToExtract;
    }
}