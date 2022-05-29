/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.latin.utils;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.Subtype;
import rkr.simplekeyboard.inputmethod.latin.common.LocaleUtils;

/**
 * Utility methods for building subtypes for the supported locales.
 */
public final class SubtypeLocaleUtils {

    private SubtypeLocaleUtils() {
        // This utility class is not publicly instantiable.
    }

    private static final String LOCALE_ENGLISH_UNITED_STATES = "en_US";

    private static final String[] sSupportedLocales = new String[] {
            LOCALE_ENGLISH_UNITED_STATES,
    };

    /**
     * Get a list of all of the currently supported subtype locales.
     * @return a list of subtype strings in the format of "ll_cc_variant" where "ll" is a language
     * code, "cc" is a country code.
     */
    public static List<String> getSupportedLocales() {
        return Arrays.asList(sSupportedLocales);
    }

    public static final String LAYOUT_QWERTY = "qwerty";

    /**
     * Get a list of all of the supported subtypes for a locale.
     * @param locale the locale string for the subtypes to look up.
     * @param resources the resources to use.
     * @return the list of subtypes for the specified locale.
     */
    public static List<Subtype> getSubtypes(final String locale, final Resources resources) {
        return new SubtypeBuilder(locale, true, resources).getSubtypes();
    }

    /**
     * Get the default subtype for a locale.
     * @param locale the locale string for the subtype to look up.
     * @param resources the resources to use.
     * @return the default subtype for the specified locale or null if the locale isn't supported.
     */
    public static Subtype getDefaultSubtype(final String locale, final Resources resources) {
        final List<Subtype> subtypes = new SubtypeBuilder(locale, true, resources).getSubtypes();
        return subtypes.size() == 0 ? null : subtypes.get(0);
    }

    /**
     * Get a subtype for a specific locale and keyboard layout.
     * @param locale the locale string for the subtype to look up.
     * @param layoutSet the keyboard layout set name for the subtype.
     * @param resources the resources to use.
     * @return the subtype for the specified locale and layout or null if it isn't supported.
     */
    public static Subtype getSubtype(final String locale, final String layoutSet,
                                     final Resources resources) {
        final List<Subtype> subtypes =
                new SubtypeBuilder(locale, layoutSet, resources).getSubtypes();
        return subtypes.size() == 0 ? null : subtypes.get(0);
    }

    /**
     * Get the list subtypes corresponding to the system's languages.
     * @param resources the resources to use.
     * @return the default list of subtypes based on the system's languages.
     */
    public static List<Subtype> getDefaultSubtypes(final Resources resources) {
        final ArrayList<Locale> supportedLocales = new ArrayList<>(sSupportedLocales.length);
        for (final String localeString : sSupportedLocales) {
            supportedLocales.add(LocaleUtils.constructLocaleFromString(localeString));
        }

        final List<Locale> systemLocales = LocaleUtils.getSystemLocales();

        final ArrayList<Subtype> subtypes = new ArrayList<>();
        final HashSet<Locale> addedLocales = new HashSet<>();
        for (final Locale systemLocale : systemLocales) {
            final Locale bestLocale = LocaleUtils.findBestLocale(systemLocale, supportedLocales);
            if (bestLocale != null && !addedLocales.contains(bestLocale)) {
                addedLocales.add(bestLocale);
                final String bestLocaleString = LocaleUtils.getLocaleString(bestLocale);
                subtypes.add(getDefaultSubtype(bestLocaleString, resources));
            }
        }
        if (subtypes.size() == 0) {
            // there needs to be at least one default subtype
            subtypes.add(getSubtypes(LOCALE_ENGLISH_UNITED_STATES, resources).get(0));
        }
        return subtypes;
    }

    /**
     * Utility for building the supported subtype objects. {@link #getSubtypes} sets up the full
     * list of available subtypes for a locale, but not all of the subtypes that it requests always
     * get returned. The parameters passed in the constructor limit what subtypes are actually built
     * and returned. This allows for a central location for indicating what subtypes are available
     * for each locale without always needing to build them all.
     */
    private static class SubtypeBuilder {
        private final Resources mResources;
        private final boolean mAllowMultiple;
        private final String mLocale;
        private final String mExpectedLayoutSet;
        private List<Subtype> mSubtypes;

        /**
         * Builder for single subtype with a specific locale and layout.
         * @param locale the locale string for the subtype to build.
         * @param layoutSet the keyboard layout set name for the subtype.
         * @param resources the resources to use.
         */
        public SubtypeBuilder(final String locale, final String layoutSet,
                              final Resources resources) {
            mLocale = locale;
            mExpectedLayoutSet = layoutSet;
            mAllowMultiple = false;
            mResources = resources;
        }

        /**
         * Builder for one or all subtypes with a specific locale.
         * @param locale the locale string for the subtype to build.
         * @param all true to get all of the subtypes for the locale or false for just the default.
         * @param resources the resources to use.
         */
        public SubtypeBuilder(final String locale, final boolean all, final Resources resources) {
            mLocale = locale;
            mExpectedLayoutSet = null;
            mAllowMultiple = all;
            mResources = resources;
        }

        /**
         * Get the requested subtypes.
         * @return the list of subtypes that were built.
         */
        public List<Subtype> getSubtypes() {
            if (mSubtypes != null) {
                // in case this gets called again for some reason, the subtypes should only be built
                // once
                return mSubtypes;
            }
            mSubtypes = new ArrayList<>();
            // This should call to build all of the available for each supported locale. The private
            // helper functions will handle skipping building the subtypes that weren't requested.
            // The first subtype that is specified to be built here for each locale will be
            // considered the default.
            /*
            switch (mLocale) {
                case LOCALE_ENGLISH_UNITED_STATES:
                    addLayout(LAYOUT_QWERTY);
                    addGenericLayouts();
                    break;
            }
            */
            addLayout(LAYOUT_QWERTY);
            addGenericLayouts();
            return mSubtypes;
        }

        /**
         * Check if the layout should skip being built based on the request from the constructor.
         * @param keyboardLayoutSet the layout set for the subtype to potentially build.
         * @return whether the subtype should be skipped.
         */
        private boolean shouldSkipLayout(final String keyboardLayoutSet) {
            if (mAllowMultiple) {
                return false;
            }
            if (mSubtypes.size() > 0) {
                return true;
            }
            if (mExpectedLayoutSet != null) {
                return !mExpectedLayoutSet.equals(keyboardLayoutSet);
            }
            return false;
        }

        /**
         * Add a single layout for the locale. This might not actually add the subtype to the list
         * depending on the original request.
         * @param keyboardLayoutSet the keyboard layout set name.
         */
        private void addLayout(final String keyboardLayoutSet) {
            if (shouldSkipLayout(keyboardLayoutSet)) {
                return;
            }

            // if this is a generic layout, use that corresponding layout name
            final String[] predefinedLayouts =
                    mResources.getStringArray(R.array.predefined_layouts);
            final int predefinedLayoutIndex =
                    Arrays.asList(predefinedLayouts).indexOf(keyboardLayoutSet);
            final String layoutNameStr;
            if (predefinedLayoutIndex >= 0) {
                final String[] predefinedLayoutDisplayNames = mResources.getStringArray(
                        R.array.predefined_layout_display_names);
                layoutNameStr = predefinedLayoutDisplayNames[predefinedLayoutIndex];
            } else {
                layoutNameStr = null;
            }

            mSubtypes.add(
                    new Subtype(mLocale, keyboardLayoutSet, layoutNameStr, false, mResources));
        }

        /**
         * Add a single layout for the locale. This might not actually add the subtype to the list
         * depending on the original request.
         * @param keyboardLayoutSet the keyboard layout set name.
         * @param layoutRes the resource ID to use for the display name of the keyboard layout. This
         *                 generally shouldn't include the name of the language.
         */
        private void addLayout(final String keyboardLayoutSet, final int layoutRes) {
            if (shouldSkipLayout(keyboardLayoutSet)) {
                return;
            }
            mSubtypes.add(
                    new Subtype(mLocale, keyboardLayoutSet, layoutRes, true, mResources));
        }

        /**
         * Add the predefined layouts (eg: QWERTY, AZERTY, etc) for the locale. This might not
         * actually add all of the subtypes to the list depending on the original request.
         */
        private void addGenericLayouts() {
            if (mSubtypes.size() > 0 && !mAllowMultiple) {
                return;
            }
            final int initialSize = mSubtypes.size();
            final String[] predefinedKeyboardLayoutSets = mResources.getStringArray(
                    R.array.predefined_layouts);
            final String[] predefinedKeyboardLayoutSetDisplayNames = mResources.getStringArray(
                    R.array.predefined_layout_display_names);
            for (int i = 0; i < predefinedKeyboardLayoutSets.length; i++) {
                final String predefinedLayout = predefinedKeyboardLayoutSets[i];
                if (shouldSkipLayout(predefinedLayout)) {
                    continue;
                }

                boolean alreadyExists = false;
                for (int subtypeIndex = 0; subtypeIndex < initialSize; subtypeIndex++) {
                    final String layoutSet = mSubtypes.get(subtypeIndex).getKeyboardLayoutSet();
                    if (layoutSet.equals(predefinedLayout)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (alreadyExists) {
                    continue;
                }

                mSubtypes.add(new Subtype(mLocale, predefinedLayout,
                        predefinedKeyboardLayoutSetDisplayNames[i], true, mResources));
            }
        }
    }
}
