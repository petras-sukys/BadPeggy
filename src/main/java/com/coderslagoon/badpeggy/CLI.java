package com.coderslagoon.badpeggy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.coderslagoon.badpeggy.scanner.ImageFormat;
import com.coderslagoon.badpeggy.scanner.ImageScanner;
import com.coderslagoon.baselib.io.FileNode;
import com.coderslagoon.baselib.io.FileRegistrar;
import com.coderslagoon.baselib.io.FileRegistrar.Callback.Merge;
import com.coderslagoon.baselib.io.FileRegistrar.InMemory.DefCmp;
import com.coderslagoon.baselib.io.FileSystem;
import com.coderslagoon.baselib.io.LocalFileSystem;
import com.coderslagoon.baselib.util.MiscUtils;

public class CLI {

    private FileSystem.Filter searchFilter;
    ExecutorService   exec;
    static final int IO_BUF_SZ = 0x10000;

    long total_files = 0;
    long scanned_files = 0;
    long printed_percent = 0;

    public void run(String pathToScan) {
        this.exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        FileRegistrar freg = new FileRegistrar.InMemory(new DefCmp(false));
        final String[] exts = MiscUtils.csvLoad(GUIProps.OPTS_FILEEXTS.get(), true);
        for (int i = 0; i < exts.length; i++) {
            exts[i] = "." + exts[i].toLowerCase();
        }
        this.searchFilter = fn -> {
            if (fn.hasAttributes(FileNode.ATTR_DIRECTORY)) {
                return true;
            }
            final String nm = fn.name().toLowerCase();
            for(String ext : exts) {
                if (nm.endsWith(ext)) {
                    return true;
                }
            }
            return false;
            };

        FileSystem fs = new LocalFileSystem(false);
        try {
            FileRegistrar.Callback frcb = (nd0, nd1) ->  Merge.IGNORE;
            List<FileNode> files = new ArrayList<>();
            FileNode fn = fs.nodeFromString(pathToScan);
            if (fn.hasAttributes(FileNode.ATTR_DIRECTORY)) {
                search(freg, fs, fn, fn, true);
            }
            else {
                files.clear();
                files.add(fn);
                freg.add(files, fn, freg.root(), frcb);
                this.total_files += 1;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        scanDirectory(freg.root());

        this.exec.shutdown();
        try {
            this.exec.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    boolean scanDirectory(FileRegistrar.Directory dir) {
        Iterator<FileNode> itf = dir.files();
        while (itf.hasNext()) {
            this.exec.execute(new ScanRun(itf.next()));
        }
        Iterator<FileRegistrar.Directory> itd = dir.dirs();
        while (itd.hasNext()) {
            if (!scanDirectory(itd.next())) {
                return false;
            }
        }
        return true;
    }

    int search(FileRegistrar freg, FileSystem fs, FileNode dir, FileNode bottom, boolean recursive) throws IOException {

        Iterator<FileNode> ifn = fs.list(dir, this.searchFilter);
        List<FileNode> files = new ArrayList<>();
        int result = 0;
        while (ifn.hasNext()) {
            FileNode fn = ifn.next();
            if (fn.hasAttributes(FileNode.ATTR_DIRECTORY)) {
                if (recursive) {
                    result += search(freg, fs, fn, bottom, recursive);
                }
            }
            else {
                files.add(fn);
                this.total_files += 1;
                result++;
            }
        }
        freg.add(files, bottom, null, (nd0, nd1) -> Merge.IGNORE);
        return result;
    }

    class ScanRun implements Runnable {
        final FileNode fnode;
        public ScanRun(FileNode fnode) {
            this.fnode = fnode;
        }
        public void run() {
            try {
                run2();
            }
            catch (Throwable uncaught) {
                uncaught.printStackTrace();
            }
        }
        public void run2() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            InputStream ins = null;
            try {
                String filePath = this.fnode.path(true);
                ImageScanner.InputStreamSource iss = new ImageScanner.InputStreamSource() {
                    public InputStream get() throws IOException{
                        InputStream ins = ScanRun.this.fnode.fileSystem().openRead(ScanRun.this.fnode);
                        return new BufferedInputStream(ins, IO_BUF_SZ);
                    }
                };
                ImageScanner scanner = new ImageScanner();
                scanner.log.info(filePath);
                if (null == scanner.scan(iss,
                    ImageFormat.fromFileName(this.fnode.name()),
                    percent -> true)) {
                    throw new Exception("missing JPEG reader");
                }
                updateResult(filePath, scanner.lastResult());
            }
            catch (Throwable err) {
                err.printStackTrace();
            }
            finally {
                if (null != ins) {
                    try { ins.close(); } catch (Exception ignored) { }
                }
            }
        }
    }

    synchronized void updateResult(final String fpath, final ImageScanner.Result res) {
        this.scanned_files += 1;
        long percent = Math.round(100.0*scanned_files/total_files);
        switch(res.type()) {
            case OK: {
                if (printed_percent < percent) {
                    System.out.println("Progress: " + scanned_files + " / " + total_files);
                    this.printed_percent = percent;
                }
                break;
            }
            case WARNING: {
                System.out.println("W:\t " + fpath + " (" + res.collapsedMessages().toString() + ")");
                break;
            }
            case ERROR: {
                System.out.println("E:\t " + fpath + " (" + res.collapsedMessages().toString() + ")");
                break;
            }
            case UNEXPECTED_ERROR: {
                System.out.println("X:\t " + fpath + " (" + res.collapsedMessages().toString() + ")");
                break;
            }
            default: {
                throw new Error();
            }
        }
    }
}