#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint releep_scale_connect.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'releep_scale_connect'
  s.version          = '0.0.1'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.platform = :ios, '9.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }

   # Combine xcconfig for all frameworks
    s.xcconfig = {
      'OTHER_LDFLAGS' => '-framework InetBleSDK'
    }

    # Include vendored frameworks
    s.vendored_frameworks = [
      'InetBleSDK.framework'
    ]


end
