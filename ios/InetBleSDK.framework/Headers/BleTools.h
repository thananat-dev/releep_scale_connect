//
//  BleTools.h
//  GreatBody
//
//  Created by steven wu on 2019/6/25.
//

#import <Foundation/Foundation.h>

@interface BleTools : NSObject


/**
 得到当前时间,年月日时分秒
 */
+ (NSString *)returnCurrentTime;

/**
 计算两个时间的差值，单位：秒
 */
+ (NSString *)getTimeInterval:(NSString *)startTime end:(NSString *)endTime;

/**
 十进制整数byte(0-255)转二进制字符串，如 255 =》@"11111111"
 */
+ (NSString *)returnBinaryStringWithByte:(Byte)byte;
/**
 二进制字符串 =》 十进制整数字符串，如 @"11111111" =》@"255"
 */
+ (NSString *)toDecimalSystemWithBinarySystem:(NSString *)binary;

/**
蓝牙部分使用判断版本
*/
+ (NSInteger)compareDate:(NSString*)aDate withDate:(NSString*)bDate;


///将传入的data转换为16进制字符串格式<xxxx xxxx>
/// IOS13以后苹果修改了NSData的-description实现方式
/// iOS13之前：<ac02fa02 0000ccc8>
/// iOS13之后：{length = 8, bytes = 0xac02fa020000ccc8}
+ (NSString *)hexStrFromData:(NSData *)data;


#pragma mark ============ 根据给定的原始kg整数(如565)、单位、小数点，转换为要显示的值(如56.5或8:12.6) ==============

///getKg
+ (NSString *)getKg_withKgSum:(NSString *)kgIntStr kgOriginDecimal:(NSInteger)kgOriginDecimal kgShowDecimal:(NSInteger)kgShowDecimal kggraduation:(NSInteger)kg_graduation lbgraduation:(NSInteger)lb_graduation isJin:(BOOL)jin;

///getLb
+ (NSString *)getLb_withKgSum:(NSString *)kgIntStr kgOriginDecimal:(NSInteger)kgOriginDecimal lbShowDecimal:(NSInteger)lbShowDecimal kggraduation:(NSInteger)kg_graduation lbgraduation:(NSInteger)lb_graduation;

///getSt
+ (NSString *)getSt_withKgSum:(NSString *)kgIntStr kgOriginDecimal:(NSInteger)kgOriginDecimal stShowDecimal:(NSInteger)stShowDecimal kggraduation:(NSInteger)kg_graduation lbgraduation:(NSInteger)lb_graduation;

///getJin
+ (NSString *)getJin_withKgSum:(NSString *)kgIntStr kgOriginDecimal:(NSInteger)kgOriginDecimal kgShowDecimal:(NSInteger)kgShowDecimal kggraduation:(NSInteger)kg_graduation lbgraduation:(NSInteger)lb_graduation isJin:(BOOL)jin;

@end

