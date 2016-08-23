/*
 *  Copyright 2015 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ametys.runtime.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Exclusive lock on the Ametys Home directory.
 * Based on the Jackrabbit repository lock. 
 */
public class AmetysHomeLock
{
    /**
     * Name of the lock file within a directory.
     */
    private static final String __LOCK_FILE = ".lock";
    
    /** The ametys home directory */
    private File _directory;

    /** The lock file within the directory */
    private File _file;

    /** The random access file. */
    private RandomAccessFile _randomAccessFile;

    /**
     * Unique identifier (canonical path name) of the locked directory.
     * Used to ensure exclusive locking within a single JVM.
     */
    private String _identifier;

    /**
     * The file lock. Used to ensure exclusive locking across process
     * boundaries.
     */
    private FileLock _lock;

    /**
     * Default constructor.
     * Initialize the instance. The lock still needs to be
     * explicitly acquired using the {@link #acquire()} method.
     * @param ametysHome The ametys home directory
     * @throws AmetysHomeLockException if the canonical path of the directory can not be determined
     */
    public AmetysHomeLock(File ametysHome) throws AmetysHomeLockException
    {
        try
        {
            _directory = ametysHome.getCanonicalFile();
            _file = new File(_directory, __LOCK_FILE);
            _identifier = (AmetysHomeLock.class.getName() + ":" + _directory.getPath()).intern();
            _lock = null;
        }
        catch (IOException e)
        {
            throw new AmetysHomeLockException("Unable to construct the Ametys home lock instance at path " + ametysHome.getPath(), e);
        }
    }

    /**
     * Lock the Ametys home directory
     * @throws AmetysHomeLockException if the repository lock can not be acquired
     */
    public void acquire() throws AmetysHomeLockException
    {
        if (_file.exists())
        {
            System.out.println("[WARN] Existing lock file " + _file + " detected. Previous lock was not released properly.");
        }
        try
        {
            _tryLock();
        }
        catch (AmetysHomeLockException e)
        {
            _closeRandomAccessFile();
            throw e;
        }
    }

    /**
     * Try to lock the random access file.
     * @throws AmetysHomeLockException If an error occurs during the lock attempt.
     */
    private void _tryLock() throws AmetysHomeLockException
    {
        try
        {
            _randomAccessFile = new RandomAccessFile(_file, "rw");
            _lock = _randomAccessFile.getChannel().tryLock();
        }
        catch (IOException e)
        {
            throw new AmetysHomeLockException("Unable to create or lock file " + _file, e);
        }
        catch (OverlappingFileLockException e)
        {
            // OverlappingFileLockException with JRE 1.6
            throw new AmetysHomeLockException("The Ametys home " + _directory + " appears to be in use since the file named " + _file.getName()
                    + " is already locked by the current process.");
        }
        
        if (_lock == null)
        {
            throw new AmetysHomeLockException("The Ametys home " + _directory + " appears to be in use since the file named " + _file.getName()
                    + " is locked by another process.");
        }
        
        // due to a bug in java 1.4/1.5 on *nix platforms
        // it's possible that java.nio.channels.FileChannel.tryLock()
        // returns a non-null FileLock object although the lock is already
        // held by *this* jvm process
        synchronized (_identifier)
        {
            if (null != System.getProperty(_identifier))
            {
                // note that the newly acquired (redundant) file lock
                // is deliberately *not* released because this could
                // potentially cause, depending on the implementation,
                // the previously acquired lock(s) to be released
                // as well
                throw new AmetysHomeLockException("The Ametys home " + _directory + " appears to be" + " already locked by the current process.");
            }
            else
            {
                try
                {
                    System.setProperty(_identifier, _identifier);
                }
                catch (SecurityException e)
                {
                    System.out.println("[WARN] Unable to set system property: " + _identifier);
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Close the random access file if it is open, and set it to null.
     */
    private void _closeRandomAccessFile()
    {
        if (_randomAccessFile != null)
        {
            try
            {
                _randomAccessFile.close();
            }
            catch (IOException e)
            {
                System.out.println("[WARN] Unable to close the random access file " + _file);
                e.printStackTrace();
            }
            _randomAccessFile = null;
        }
    }
    
    /**
     * Releases repository lock.
     */
    public void release()
    {
        if (_lock != null)
        {
            try
            {
                try (FileChannel channel = _lock.channel())
                {
                    _lock.release();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
            
            _lock = null;
            _closeRandomAccessFile();
        }

        if (!_file.delete())
        {
            System.out.println("[WARN] Unable to delete repository lock file");
        }

        // see #acquire()
        synchronized (_identifier)
        {
            try
            {
                System.getProperties().remove(_identifier);
            }
            catch (SecurityException e)
            {
                System.out.println("[WARN] Unable to clear system property: " + _identifier);
                e.printStackTrace();
            }
        }
    }
}
