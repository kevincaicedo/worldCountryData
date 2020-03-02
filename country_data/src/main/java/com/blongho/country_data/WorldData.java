/*
 * MIT License
 *
 * Copyright (c) 2019 Bernard Che Longho
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blongho.country_data;
/**
 * @file WorldData.java
 * @author Bernard Che Longho (blongho)
 * @brief A class to load all the flags and countries in a map
 * <br> This eases the access of flag when the country
 * alpha2 or alpha3  or the numeric codes are known<br> This class is accessible only to the
 * package
 * @since 2019-11-15 Refactored class and removes many unnecessary variables.
 * @since 2020-02-29 Changes classname from WorldBuilder to WorldData. *Builder is misleading since
 * this class does not follow th Builder pattern
 */

import android.content.Context;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

final class WorldData {

  private static WorldData instance;
  private static PhoneNumberUtil phoneNumberUtil;
  private static Map<Country, Integer> countryFlagMap = new HashMap<>();
  private static Country universe;

  private WorldData(final Context ctx) {
    if (phoneNumberUtil == null){
      phoneNumberUtil = PhoneNumberUtil.createInstance(ctx);
    }

    loadAllData(ctx);
  }

  /**
   * Get an instance of this class<br> This is a thread-safe singleton of the class. <br> Once
   * called, all the flag resources are loaded and all countries are assigned their flags. Calling
   * this more than once has not benefit.
   *
   * @param ctx The application context (getApplicationContext())
   * @return An instance of this class
   */

  static WorldData getInstance(Context ctx) {
    if (phoneNumberUtil == null){
      phoneNumberUtil = PhoneNumberUtil.createInstance(ctx);
    }

    if (instance != null) {
      return instance;
    }
    synchronized (WorldData.class) {
      if (instance == null) {
        instance = new WorldData(ctx);
      }
    }
    return instance;
  }

  /* package */
  static List<Country> countries() {
    return new ArrayList<>(countryFlagMap.keySet());
  }

  /**
   * The Image of the globe
   *
   * @return The globe as a drawable resource
   */
  static int globe() {
    return R.drawable.globe;
  }

  /**
   * Get the flag of a country using any of the country attributes
   *
   * @param countryIdentifier (alpha2, alpha3, country name, or numeric code)
   * @return flag resource
   */
  static int flagFromCountry(final String countryIdentifier) {
    for (Country country : countryFlagMap.keySet()) {
      if (country.hasProperty(countryIdentifier)) {
        return country.getFlagResource();
      }
    }
    return globe();
  }

  static Country countryFrom(String countryIdentifier) {
    for (Country country : countryFlagMap.keySet()) {
      if (country.hasProperty(countryIdentifier)) {
        return country;
      }
    }
    return universe;
  }

  /**
   * Load the countries and their flags in a Map container
   * <br>
   * Each country is flag is mapped with the country alpha2 and alpha3 codes
   */
  private void loadAllData(Context context) {
    int countryFlag;
    Country[] countries = getCountries(context);

    for (final Country country : countries) {
      // do was not allowed as a drawable so was renamed to dominican
      if (country.getAlpha2().equalsIgnoreCase("do")) {
        countryFlag = R.drawable.dominican;

      } else {
        final String resource = "drawable/" + country.getAlpha2().toLowerCase();
        countryFlag = context.getResources()
            .getIdentifier(resource, null, context.getPackageName());
      }

      final int code = phoneNumberUtil.getCountryCodeForRegion(country.getAlpha2());
      country.setCodePhone(String.valueOf(code));
      country.setFlagResource(countryFlag);
      countryFlagMap.put(country, countryFlag);
      if (country.getAlpha2().equalsIgnoreCase("xx")) {
        universe = country;
      }
    }
  }

  private PhoneNumberUtil getPhoneNumberUtil(Context ctx){
    if (phoneNumberUtil == null){
      phoneNumberUtil = PhoneNumberUtil.createInstance(ctx);
    }

    return phoneNumberUtil;
  }
  /**
   * Read all countries from file
   */
  private Country[] getCountries(Context context) {
    final String values = AssetsReader.readFromAssets(context,
        R.raw.com_blongho_country_data_countries);
    Gson gson = new Gson();
    return gson.fromJson(values, Country[].class);
  }

}

