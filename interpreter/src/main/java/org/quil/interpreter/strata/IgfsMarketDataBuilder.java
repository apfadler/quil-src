/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package org.quil.interpreter.strata;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.opengamma.strata.collect.Messages;
import com.opengamma.strata.collect.io.ResourceLocator;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteFileSystem;
import org.apache.ignite.Ignition;
import org.apache.ignite.igfs.IgfsFile;
import org.apache.ignite.igfs.IgfsPath;

/**
 * Loads market data from the standard directory structure on disk.
 */
public class IgfsMarketDataBuilder extends QuilMarketDataBuilder {

    /**
     * The path to the root of the directory structure.
     */
    private final IgfsPath rootPath;

    /**
     * Constructs an instance.
     *
     * @param rootPath  the path to the root of the directory structure
     */
    public IgfsMarketDataBuilder(IgfsPath rootPath) {
        this.rootPath = rootPath;
    }

    //-------------------------------------------------------------------------
    @Override
    protected Collection<ResourceLocator> getAllResources(String subdirectoryName) {

        Ignite ignite = Ignition.ignite();
        IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

        ArrayList<ResourceLocator> ret = new ArrayList<ResourceLocator>();
        for (IgfsFile file : fs.listFiles(new IgfsPath(rootPath.toString()+ "/" + subdirectoryName))) {
            ret.add(ResourceLocator.ofIgfsFile(file.path().toString()));
        }

        return ret;
    }

    @Override
    protected ResourceLocator getResource(String subdirectoryName, String resourceName) {

        Ignite ignite = Ignition.ignite();
        IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

        if (!fs.exists(new IgfsPath(rootPath.toString()+"/"+subdirectoryName+"/"+resourceName))) {
            return null;
        }

        return ResourceLocator.ofIgfsFile(rootPath.toString()+"/"+subdirectoryName+"/"+resourceName);
    }

    @Override
    protected boolean subdirectoryExists(String subdirectoryName) {
        Ignite ignite = Ignition.ignite();
        IgniteFileSystem fs = ignite.fileSystem("quil-igfs");

        return fs.exists(new IgfsPath(rootPath.toString()+"/"+subdirectoryName));
    }

}
