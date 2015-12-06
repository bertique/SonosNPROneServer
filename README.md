# NPR One Sonos Service
I love my Sonos system and I really like the [NPR One app](http://www.npr.org/about/products/npr-one/). That’s why I have built this service.

Issues and pull-requests welcome.

# How do I add NPR One to my Sonos?
Check out the instructions at [http://michaeldick.me/sonos-nprone/](http://michaeldick.me/sonos-nprone/)

# How do I run the service myself?

## Prerequsists
* [NPR One app registration](dev.npr.org)
* [Heroku account (or similar)](heroku.com)

## Run the service locally
* Clone this repo
* Import into Eclipse as Maven project
* Create new Maven build configuration with environment variables:
 * NPR_CLIENT_ID
 * NPR_CLIENT_SECRET
 * KEEN_PROJECT_ID
 * KEEN_READ_KEY
 * KEEN_WRITE_KEY
 * KEEN_ENV
* Run Maven target: *clean package tomcat7:run-war*

## Run service on Heroku
* Create new Heroku app
* Add Keen add-in (optional)
* Set environment variables (see above)
* Git push to Heroku. It will use the included procfile