package org.piwigo;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

/**
 * This is a custom version of the {@link RobolectricGradleTestRunner} that adds support for data
 * binding library
 *
 * @see <a href="https://philio.me/android-data-binding-with-robolectric-3/">https://philio.me/android-data-binding-with-robolectric-3/</a>
 */
public class RobolectricDataBindingTestRunner extends RobolectricGradleTestRunner {

    private static final String BUILD_OUTPUT = "build/intermediates";

    public RobolectricDataBindingTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        if (config.constants() == Void.class) {
            Logger.error("Field 'constants' not specified in @Config annotation");
            Logger.error("This is required when using RobolectricGradleTestRunner!");
            throw new RuntimeException("No 'constants' field in @Config annotation!");
        }

        final String type = getType(config);
        final String flavor = getFlavor(config);
        final String applicationId = getApplicationId(config);

        final FileFsFile res;
        if (FileFsFile.from(BUILD_OUTPUT, "res", flavor, type).exists()) {
            res = FileFsFile.from(BUILD_OUTPUT, "res", flavor, type);
        } else {
            // Use res/merged if the output directory doesn't exist for Data Binding compatibility
            res = FileFsFile.from(BUILD_OUTPUT, "res/merged", flavor, type);
        }
        final FileFsFile assets = FileFsFile.from(BUILD_OUTPUT, "assets", flavor, type);

        final FileFsFile manifest;
        if (FileFsFile.from(BUILD_OUTPUT, "manifests").exists()) {
            manifest = FileFsFile.from(BUILD_OUTPUT, "manifests", "full", flavor, type, "AndroidManifest.xml");
        } else {
            // Fallback to the location for library manifests
            manifest = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "AndroidManifest.xml");
        }

        Logger.debug("Robolectric assets directory: " + assets.getPath());
        Logger.debug("   Robolectric res directory: " + res.getPath());
        Logger.debug("   Robolectric manifest path: " + manifest.getPath());
        Logger.debug("    Robolectric package name: " + applicationId);
        return new AndroidManifest(manifest, res, assets, applicationId);
    }

    private String getType(Config config) {
        try {
            return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
        } catch (Throwable e) {
            return null;
        }
    }

    private String getFlavor(Config config) {
        try {
            return ReflectionHelpers.getStaticField(config.constants(), "FLAVOR");
        } catch (Throwable e) {
            return null;
        }
    }

    private String getApplicationId(Config config) {
        try {
            return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
        } catch (Throwable e) {
            return null;
        }
    }

}
