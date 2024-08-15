#import "ReleepScaleConnectPlugin.h"
#import <InetBleSDK/InetBleSDK.h>
#import "AppUser.h"


@interface ReleepScaleConnectPlugin() <INBluetoothManagerDelegate, FlutterStreamHandler>

@property (nonatomic, strong) NSMutableArray *peripheralArray;
@property (nonatomic, assign) BOOL isAddPeripheraling;
@property (nonatomic, copy) FlutterEventSink eventSink;
@property (nonatomic, copy) FlutterResult methodCallResult;
@property (nonatomic, strong) UserInfoModel *currentInfoModel;
@property (nonatomic, strong) DeviceModel *targetDeviceModel;

@property (nonatomic, strong) AppUser *appUser;


@end

@implementation ReleepScaleConnectPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* methodChannel = [FlutterMethodChannel
        methodChannelWithName:@"releep_scale_connect"
              binaryMessenger:[registrar messenger]];
    ReleepScaleConnectPlugin* instance = [[ReleepScaleConnectPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:methodChannel];
    
    FlutterEventChannel* eventChannel = [FlutterEventChannel eventChannelWithName:@"scan_releep_scale"binaryMessenger:[registrar messenger]];
    [eventChannel setStreamHandler:instance];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"getPlatformVersion" isEqualToString:call.method]) {
        result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
    }else if ([@"connectReleepScale" isEqualToString:call.method]) {
        self.methodCallResult = result;
        _appUser = [[AppUser alloc] init];
        _appUser.sex = [call.arguments[@"sex"] intValue];
        _appUser.age = [call.arguments[@"age"] intValue];
        _appUser.height = [call.arguments[@"height"] intValue];
        _appUser.weightKg = 0.0;
        _appUser.adc = 0;
        NSString *macAddress = call.arguments[@"releepScaleMac"];
        [self connectReleepScaleWithMacAddress:macAddress result:result];
    } else {
        result(FlutterMethodNotImplemented);
    }
}

#pragma mark - FlutterStreamHandler Methods

- (FlutterError *)onListenWithArguments:(id)arguments eventSink:(FlutterEventSink)events {
    self.eventSink = events;

    if ([arguments isKindOfClass:[NSString class]] && [arguments isEqualToString:@"scan"]) {
         [self startScanning];
     } else {
         NSLog(@"Received unexpected arguments: %@", arguments);
         return [FlutterError errorWithCode:@"INVALID_ARGUMENT"
                                    message:@"Expected 'listeningdataIOS' as argument."
                                    details:nil];
     }
    return nil;
}

- (FlutterError *)onCancelWithArguments:(id)arguments {
    self.eventSink = nil;
    [self stopScanning];
    return nil;
}


#pragma mark - Connect Scale Method

- (void)connectReleepScaleWithMacAddress:(NSString *)macAddress result:(FlutterResult)result {
    // Find the DeviceModel with the given macAddress in peripheralArray
    [AnalysisBLEDataManager shareManager].infoDelegate = self;
    [INBluetoothManager shareManager].delegate = self;
    [INBluetoothManager enableSDKLogs:YES]; //open log switch
    DeviceModel *deviceToConnect = nil;
    for (DeviceModel *model in self.peripheralArray) {
        if ([model.deviceAddress isEqualToString:macAddress]) {
            deviceToConnect = model;
            break;
        }
    }
    
    if (deviceToConnect) {
        // Perform connection logic using the INBluetoothManager
        [self connectToDevice:deviceToConnect result:result];
    } else {
        // Device not found
        result([FlutterError errorWithCode:@"DEVICE_NOT_FOUND"
                                   message:[NSString stringWithFormat:@"Device with MAC address %@ not found.", macAddress]
                                   details:nil]);
    }
}

- (void)connectToDevice:(DeviceModel *)device result:(FlutterResult)result {
    // Ensure the Bluetooth manager is available
    if ([INBluetoothManager shareManager].bleState == CBManagerStatePoweredOn) {
        // Start connection process
        [[INBluetoothManager shareManager] connectToLinkScale:device];
        
        // Example of handling success or failure
        // You should set up appropriate delegate methods or callbacks to handle the connection result
        // For example, you might need to set up delegate methods to handle the connection completion
//        self.connectionResult = result;
    } else {
        result([FlutterError errorWithCode:@"BLE_UNAVAILABLE"
                                   message:@"Bluetooth is not available on this device."
                                   details:nil]);
    }
}

- (void)AnalysisBLEDataManager:(AnalysisBLEDataManager *)analysisManager updateBleDataAnalysisStatus:(BleDataAnalysisStatus)bleDataAnalysisStatus {
    switch (bleDataAnalysisStatus) {
        case BleDataAnalysisStatus_SyncTimeSuccess:
            NSLog(@"sync time success");
            if (self.methodCallResult) {
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [self syncWeighingUserToBle];
                });
                self.methodCallResult(@(0));
                self.methodCallResult = nil;
            }
            break;
        case BleDataAnalysisStatus_SyncTimeFailed:
            NSLog(@"sync time failed");
            break;
        case BleDataAnalysisStatus_SyncUserSuccess:
            NSLog(@"sync weighing user success");
            break;
        case BleDataAnalysisStatus_SyncUserFailed:
            NSLog(@"sync weighing user failed");
            break;
        case BleDataAnalysisStatus_UnstableWeight:
            NSLog(@"measuring...\nUnstable Weight");
            break;
        case BleDataAnalysisStatus_StableWeight:
            NSLog(@"Stable Weight");
            break;
        case BleDataAnalysisStatus_MeasureComplete:
            NSLog(@"measure complete");
            break;
        case BleDataAnalysisStatus_AdcError:
            NSLog(@"adc measure failed");
            break;
        case BleDataAnalysisStatus_LightOff:
            NSLog(@"your linkScale light off");
            break;
        default:
            break;
    }
}

// click sync user button
- (void)syncWeighingUserToBle {
    
    if (_targetDeviceModel.acNumber.intValue < 2) {
        //broadcast scale do not need to sync user
    } else {
        //connect scale must input sex、weight、age
        
        BLEUser *user = [[BLEUser alloc] init];
        user.userSex = _appUser.sex;
        user.userAge = _appUser.age;
        user.userHeight = _appUser.height;
        [[WriteToBLEManager shareManager] syncWeighingUser:user];
        
    }
}




- (void)AnalysisBLEDataManager:(AnalysisBLEDataManager *)analysisManager updateMeasureUserInfo:(UserInfoModel *)infoModel {
    NSLog(@"---infoModel:%@", infoModel);
    
    
    _currentInfoModel = infoModel;
 
    float weight = _currentInfoModel.weightsum / pow(10, _currentInfoModel.weightOriPoint); // 6895 -> 68.95
    

    if ( _currentInfoModel.weightsum > 0 && _currentInfoModel.newAdc > 0) { // Measure Complete
        
        float adc = _currentInfoModel.newAdc;
        _appUser.weightKg = weight;
        _appUser.adc = adc;

        if (_targetDeviceModel.acNumber.intValue < 2) {
            AlgorithmModel *algModel = [AlgorithmSDK getBodyfatWithWeight:weight adc:adc sex:_appUser.sex age:_appUser.age height:_appUser.height];
            _currentInfoModel.weightsum = _currentInfoModel.weightsum / pow(10, _currentInfoModel.weightOriPoint);
            _currentInfoModel.newAdc = _currentInfoModel.newAdc;
            _currentInfoModel.fatRate = algModel.bfr.floatValue;
            _currentInfoModel.BMI = algModel.bmi.floatValue;
            _currentInfoModel.moisture = algModel.vwc.floatValue;
            _currentInfoModel.muscle = algModel.rom.floatValue;
            _currentInfoModel.BMR = algModel.bmr.floatValue;
            _currentInfoModel.boneMass = algModel.bm.floatValue;
            _currentInfoModel.visceralFat = algModel.uvi.floatValue;
            _currentInfoModel.proteinRate = algModel.pp.floatValue;
            _currentInfoModel.physicalAge = algModel.physicalAge.floatValue;
            _currentInfoModel.subcutaneousFat = algModel.sfr.floatValue;
            
            NSLog(@"---BM15 AlgorithmModel: %@", _currentInfoModel);

        } else {
            // Handle the case where acNumber >= 2
            [self syncWeighingUserToBle];
            
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [self syncOfflineUserListToBle];
            });
            
            if (self.eventSink) {
                      NSError *error;
                      NSDictionary *modelDict = [self convertCurrentInfoModelToDictionary:_currentInfoModel];
                      
                      NSData *jsonData = [NSJSONSerialization dataWithJSONObject:modelDict
                                                                         options:NSJSONWritingPrettyPrinted
                                                                           error:&error];
                      
                      if (jsonData) {
                          NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                          self.eventSink(jsonString);
                      } else {
                          NSLog(@"Failed to serialize _currentInfoModel to JSON: %@", error);
                          self.eventSink([FlutterError errorWithCode:@"JSON_ERROR"
                                                             message:@"Failed to serialize _currentInfoModel to JSON."
                                                             details:error.localizedDescription]);
                      }
                  }
        }
    }else {
        if (self.eventSink) {
                  NSError *error;
                  NSDictionary *modelDict = [self convertCurrentInfoModelToDictionary:_currentInfoModel];
                  
                  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:modelDict
                                                                     options:NSJSONWritingPrettyPrinted
                                                                       error:&error];
                  
                  if (jsonData) {
                      NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                      self.eventSink(jsonString);
                  } else {
                      NSLog(@"Failed to serialize _currentInfoModel to JSON: %@", error);
                      self.eventSink([FlutterError errorWithCode:@"JSON_ERROR"
                                                         message:@"Failed to serialize _currentInfoModel to JSON."
                                                         details:error.localizedDescription]);
                  }
              }
    }
}


- (NSDictionary *)convertCurrentInfoModelToDictionary:(UserInfoModel *)model {
    return @{
        @"weightsum": @(model.weightsum),
        @"weightOriPoint": @(model.weightOriPoint),
        @"measureStatus": @(model.measureStatus),
        @"newAdc": @(model.newAdc),
        @"fatRate": @(model.fatRate),
        @"BMI": @(model.BMI),
        @"moisture": @(model.moisture),
        @"muscle": @(model.muscle),
        @"BMR": @(model.BMR),
        @"boneMass": @(model.boneMass),
        @"visceralFat": @(model.visceralFat),
        @"proteinRate": @(model.proteinRate),
        @"physicalAge": @(model.physicalAge),
        @"subcutaneousFat": @(model.subcutaneousFat)
        // Add other properties as needed
    };
}

/// If no need offline history function, do not call this method
- (void)syncOfflineUserListToBle {
    
    if (_targetDeviceModel.acNumber.intValue < 2) {
        //broadcast scale can not receive write command
    } else {
        BLEUser *user = [[BLEUser alloc] init];
        user.userSex = _appUser.sex;
        user.userAge = _appUser.age;
        user.userHeight = _appUser.height;
        user.userWeight = _appUser.weightKg; //note
        user.userAdc = _appUser.adc;         //note
        [[WriteToBLEManager shareManager] sendOfflineUserListToBle:@[user]]; //you can add more than one user to array
    }
    
}


- (void)BluetoothManager:(INBluetoothManager *)manager didConnectDevice:(DeviceModel *)deviceModel {
    _targetDeviceModel = deviceModel;
}



#pragma mark - BLE Scanning Methods

- (void)startScanning {

    if ([INBluetoothManager shareManager].bleState == CBManagerStatePoweredOn) {
        self.peripheralArray = [NSMutableArray array];
        [INBluetoothManager shareManager].delegate = self;
        [[INBluetoothManager shareManager] startBleScan];
    } else {
        if (self.eventSink) {
            self.eventSink([FlutterError errorWithCode:@"BLE_UNAVAILABLE"
                                               message:@"Bluetooth is not available on this device."
                                               details:nil]);
        }
    }
}

- (void)stopScanning {
    [[INBluetoothManager shareManager] stopBleScan];
}

- (void)BluetoothManager:(INBluetoothManager *)manager didDiscoverDevice:(DeviceModel *)deviceModel {
    if (self.isAddPeripheraling) return;

    self.isAddPeripheraling = YES;
    
    BOOL willAdd = YES;
    for (DeviceModel *model in self.peripheralArray) {
        if ([model.deviceAddress isEqualToString:deviceModel.deviceAddress] &&
            [model.deviceName isEqualToString:deviceModel.deviceName]) {
            willAdd = NO;
        }
    }
    
    if (willAdd) {
        [self.peripheralArray addObject:deviceModel];
        [self sendPeripheralToFlutter:deviceModel];
    }

    self.isAddPeripheraling = NO;
}

- (void)sendPeripheralToFlutter:(DeviceModel *)deviceModel {
    if (self.eventSink) {
        NSDictionary *deviceInfo = @{
            @"name": deviceModel.deviceName ?: @"Unknown",
            @"address": deviceModel.deviceAddress ?: @"Unknown"
        };
        
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:deviceInfo
                                                           options:NSJSONWritingPrettyPrinted
                                                             error:&error];
        if (!jsonData) {
            NSLog(@"Failed to serialize deviceInfo to JSON: %@", error);
            return;
        }
        
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        
        if (self.eventSink) {
            self.eventSink(jsonString);
        }
    }
}

@end
