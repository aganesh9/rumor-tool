import ConfigParser

config = ConfigParser.RawConfigParser()
config.add_section('Section1')
config.set('Section1', 'url', 'http://urbanlegends.about.com/od/reference/a/new_uls.htm')

# Writing our configuration file to 'dataset.cfg'
with open('dataset.cfg', 'wb') as configfile:
    config.write(configfile)
