// Ported from Sky Aesthetics (MIT) https://github.com/TathanDev/SkyAesthetics (26.1); exoconfig dropped, plain defaults
package fr.tathan.sky_aesthetics.config;



public class SkyConfig {

    public String[] disabledSkies = new String[]{
            "bad_sky:sky_to_disable"
    };

    public String[] disabledDimensions = new String[]{
            "bad_mod:bad_dimension"
    };

    public boolean disableCustomCloud = false;

    public boolean disableCustomWeather = false;

    public String[] modDisablingCloudRender = new String[]{
            "distanthorizons",
    };

    public String[] modDisablingWeather = new String[]{
            "bad_weather_mod",
    };
}