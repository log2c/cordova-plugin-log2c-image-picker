#import <Cordova/CDV.h>
#import <TZImagePickerController/TZImagePickerController.h>

@interface ImagePickerPlugin : CDVPlugin<TZImagePickerControllerDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate, UIActionSheetDelegate>

/**
 * 显示图片选择
 */
- (void)showImagePicker:(CDVInvokedUrlCommand*)command;

/**
 *拍照
 */
- (void)openCamera:(CDVInvokedUrlCommand*)command;

- (void)deleteCache:(CDVInvokedUrlCommand*)command;

@end
