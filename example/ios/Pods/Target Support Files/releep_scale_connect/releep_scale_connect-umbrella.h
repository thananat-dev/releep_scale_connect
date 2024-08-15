#ifdef __OBJC__
#import <UIKit/UIKit.h>
#else
#ifndef FOUNDATION_EXPORT
#if defined(__cplusplus)
#define FOUNDATION_EXPORT extern "C"
#else
#define FOUNDATION_EXPORT extern
#endif
#endif
#endif

#import "AppUser.h"
#import "ReleepScaleConnectPlugin.h"

FOUNDATION_EXPORT double releep_scale_connectVersionNumber;
FOUNDATION_EXPORT const unsigned char releep_scale_connectVersionString[];

