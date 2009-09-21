# dependencies are generated using a strict version, don't forget to edit the dependency versions when upgrading.
merb_gems_version = "1.0.12"

# For more information about each component, please read http://wiki.merbivore.com/faqs/merb_components
dependency "merb-action-args", merb_gems_version
dependency "merb-assets", merb_gems_version  
dependency "merb-helpers", merb_gems_version 
dependency "merb-mailer", merb_gems_version  
dependency "merb-param-protection", merb_gems_version
dependency "merb-exceptions", merb_gems_version

require 'fastercsv'
require 'extlib'
require "ostruct"
require 'ftools'
require 'ferret' # sudo gem install ferret - for text indexing
