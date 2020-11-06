package com.example.n3dot1plusswupdate;

public class XAPKFile {
    public final boolean mIsMain;
    public final int mFileVersion;
    public final long mFileSize;

    XAPKFile(boolean isMain, int fileVersion, long fileSize) {
        mIsMain = isMain;
        mFileVersion = fileVersion;
        mFileSize = fileSize;
    }
}
