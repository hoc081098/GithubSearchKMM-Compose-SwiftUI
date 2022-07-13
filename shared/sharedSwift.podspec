Pod::Spec.new do |spec|
    spec.name                     = 'sharedSwift'
    spec.version                  = '0.0.1-SNAPSHOT'
    spec.homepage                 = 'Link to the Shared Module homepage'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Some description for the Shared Module'
    spec.module_name              = 'sharedSwift'

    spec.static_framework         = true
    spec.dependency 'shared'
    spec.source_files = "build/cocoapods/framework/sharedSwift/**/*.{h,m,swift}"
end
