/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.ametys.runtime.data.AmetysHomeLock;
import org.ametys.runtime.data.AmetysHomeLockException;

/**
 * Tests the RuntimeConfig
 */
public class AmetysHomeTestCase extends AbstractRuntimeTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Test the lock on the Ametys home directory.
     */
    public void testAmetysHomeLocked()
    {
        Exception e = null;
        
        try
        {
            _ametysHomeLock.acquire();
            fail("Should not be able to acquire twice.");
        }
        catch (AmetysHomeLockException ae)
        {
            e = ae;
        }
        
        assertNotNull("An ametys home lock exception must have been thrown during the lock acquire.", e);
    }
    
    
    /**
     * Various tests on the lock feature provided by Ametys Home lock.
     * @throws Exception if an error occurs
     */
    public void testFolderLock() throws Exception
    {
        String path = "test/environments/webapp1/WEB-INF/data";
        String lockFolderName = "lockFolder";
        
        // 0 - Remove lockFolder if existing
        File lockFolder = new File(path, lockFolderName);
        FileUtils.deleteDirectory(lockFolder);
        
        // 1 - Creating a lock on an nonexistent folder will fails
        AmetysHomeLock ametysHomeLock = new AmetysHomeLock(lockFolder);
        try
        {
            ametysHomeLock.acquire();
            fail("Should not be able to acquire a lock on a unexisting folder");
        }
        catch (AmetysHomeLockException e)
        {
            // ok
        }
        finally
        {
            ametysHomeLock.release();
        }
        
        // 2 - Creating the folder, the lock can be acquired now
        lockFolder.mkdirs();
        ametysHomeLock = new AmetysHomeLock(lockFolder);
        try
        {
            ametysHomeLock.acquire();
        }
        catch (AmetysHomeLockException e)
        {
            _logger.error("The lock must be acquired without error", e);
            fail("The lock must be acquired without error");
        }
        
        // 3 - Cannot remove the '.lock' file
        File lockFile = new File(lockFolder, ".lock");
        
        assertTrue("The lock must be acquired", lockFile.exists());
        assertFalse("Must not be able to create a lock file", lockFile.createNewFile());
        
        // Create a folder and a file in the locked folder
        String filePath = "a/b/c.txt";
        File file = new File(lockFolder, filePath);
        assertTrue("Must be able to create folder", file.getParentFile().mkdirs());
        assertTrue("Must be able to a file", file.createNewFile());
        
        try (PrintWriter pw = new PrintWriter(file))
        {
            pw.write("Writing in file...");
        }
        catch (Exception e)
        {
            _logger.error("Error while writing to file", e);
            fail("Must be able to write to a file");
        }
        
        assertTrue("Must be able to delete the file", file.delete());
        
        // 5 - Release the lock
        ametysHomeLock.release();
        assertFalse("The lock has been released", lockFile.exists());
        
        // 6 - Create a fake lock file in the lock folder
        assertTrue("Must be able to create a fake lock file", lockFile.createNewFile());
        
        // 7 - Must still be able to acquire the lock
        ametysHomeLock = new AmetysHomeLock(lockFolder);
        try
        {
            ametysHomeLock.acquire();
        }
        catch (AmetysHomeLockException e)
        {
            fail("The lock must be acquired without error");
        }
        finally
        {
            ametysHomeLock.release();
        }
    }
    
    /**
     * Test the acquisition of the lock on a read only folder
     * @throws Exception if an error occurs
     */
    public void testFolderLockReadOnly() throws Exception
    {
        String path = "test/environments/webapp1/WEB-INF/data";
        String lockFolderName = "lockFolder";
        
        // Remove lockFolder if existing
        File lockFolder = new File(path, lockFolderName);
        FileUtils.deleteDirectory(lockFolder);
        
        lockFolder.mkdirs();
        assertTrue("The directory must be marked as read only", _makeDirectoryReadOnly(lockFolder));
        assertFalse("The folder must be read only", Files.isWritable(lockFolder.toPath()));
        
        AmetysHomeLock ametysHomeLock = new AmetysHomeLock(lockFolder);
        try
        {
            ametysHomeLock.acquire();
            fail("Should not be able to acquire a lock on a read only folder");
        }
        catch (AmetysHomeLockException e)
        {
            // ok
        }
        finally
        {
            ametysHomeLock.release();
        }
    }

    private boolean _makeDirectoryReadOnly(File dir) throws IOException
    {
        Path path = dir.toPath();
        
        FileStore store = Files.getFileStore(path);
        boolean supportAcl = store.supportsFileAttributeView("acl"); // For Windows mainly.
        boolean supportPosix = store.supportsFileAttributeView("posix"); // For Unix mainly.
        
        if (supportAcl)
        {
            UserPrincipal current = path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(System.getProperty("user.name"));

            // get view
            AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);

            // create specific read only entry (+ delete) for current user
            AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.DENY)
                .setFlags(EnumSet.of(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT))
                .setPrincipal(current)
                .setPermissions(AclEntryPermission.WRITE_DATA, AclEntryPermission.WRITE_ATTRIBUTES)
                .build();

            List<AclEntry> acl = view.getAcl();
            acl.add(0, entry);
            view.setAcl(acl);
            
            return true;
        }
        else if (supportPosix)
        {
            // get view
            PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            view.setPermissions(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ));
            
            return true;
        }
        
        return false;
    }
}

