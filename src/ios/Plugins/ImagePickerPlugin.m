#import <Cordova/CDV.h>
#import "ImagePickerPlugin.h"

#import <TZImagePickerController/TZImagePickerController.h>
#import "NSDictionary+SYSafeConvert.h"
#import "TZImageManager.h"
#import "NSDictionary+SYSafeConvert.h"
#import "TZImageCropManager.h"
#import <AssetsLibrary/AssetsLibrary.h>

@interface ImagePickerPlugin ()

@property (nonatomic, strong) NSDictionary *cameraOptions;
/**
 保存选中的图片数组
 */
@property (nonatomic, strong) NSMutableArray *selectedAssets;

@property (nonatomic, retain) UINavigationController *navigationController;

@property (copy) NSString* callbackId;

@property (nonatomic, strong) UIImagePickerController *imagePickerVc;

@end

@implementation ImagePickerPlugin

- (instancetype)init {
    self = [super init];
    if (self) {
        _selectedAssets = [NSMutableArray array];
    }
    return self;
}

- (void)dealloc {
    _selectedAssets = nil;
}


-(void)showImagePicker:(CDVInvokedUrlCommand *)command{
    self.callbackId = command.callbackId;
    NSDictionary *options = [command.arguments objectAtIndex:0];
    self.cameraOptions = options;

    NSInteger imageCount = [options sy_integerForKey:@"imageCount"];
    BOOL isCamera        = [options sy_boolForKey:@"isCamera"];
    BOOL isCrop          = [options sy_boolForKey:@"isCrop"];
    BOOL isGif = [options sy_boolForKey:@"isGif"];
    BOOL allowPickingVideo = [options sy_boolForKey:@"allowPickingVideo"];
    BOOL allowPickingMultipleVideo = [options sy_boolForKey:@"allowPickingMultipleVideo"];
    BOOL allowPickingImage = [options sy_boolForKey:@"allowPickingImage"];
    BOOL showCropCircle  = [options sy_boolForKey:@"showCropCircle"];
    BOOL isRecordSelected = [options sy_boolForKey:@"isRecordSelected"];
    BOOL allowPickingOriginalPhoto = [options sy_boolForKey:@"allowPickingOriginalPhoto"];
    BOOL sortAscendingByModificationDate = [options sy_boolForKey:@"sortAscendingByModificationDate"];
    NSInteger CropW      = [options sy_integerForKey:@"CropW"];
    NSInteger CropH      = [options sy_integerForKey:@"CropH"];
    NSInteger circleCropRadius = [options sy_integerForKey:@"circleCropRadius"];
    NSInteger videoMaximumDuration = [options sy_integerForKey:@"videoMaximumDuration"];
    //    NSInteger quality  = [self.cameraOptions sy_integerForKey:@"quality"];

    TZImagePickerController *imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:imageCount delegate:self];

    imagePickerVc.maxImagesCount = imageCount;
    imagePickerVc.allowPickingGif = isGif; // 允许GIF
    imagePickerVc.allowTakePicture = isCamera; // 允许用户在内部拍照
    imagePickerVc.allowPickingVideo = allowPickingVideo; // 不允许视频
    imagePickerVc.allowPickingImage = allowPickingImage;
    imagePickerVc.allowTakeVideo = NO;
    imagePickerVc.videoMaximumDuration = videoMaximumDuration;
    imagePickerVc.allowPickingMultipleVideo = isGif ? YES : allowPickingMultipleVideo;
    imagePickerVc.allowPickingOriginalPhoto = allowPickingOriginalPhoto; // 允许原图
    imagePickerVc.sortAscendingByModificationDate = sortAscendingByModificationDate;
    imagePickerVc.alwaysEnableDoneBtn = YES;
    imagePickerVc.allowCrop = isCrop;   // 裁剪
    imagePickerVc.autoDismiss = YES;
    imagePickerVc.modalPresentationStyle = UIModalPresentationFullScreen;
    //
    if (isRecordSelected) {
        imagePickerVc.selectedAssets = self.selectedAssets; // 当前已选中的图片
    }

    if (imageCount == 1) {
        // 单选模式
        imagePickerVc.showSelectBtn = NO;

        if(isCrop){
            if(showCropCircle) {
                imagePickerVc.needCircleCrop = showCropCircle; //圆形裁剪
                imagePickerVc.circleCropRadius = circleCropRadius; //圆形半径
            } else {
                CGFloat x = ([[UIScreen mainScreen] bounds].size.width - CropW) / 2;
                CGFloat y = ([[UIScreen mainScreen] bounds].size.height - CropH) / 2;
                imagePickerVc.cropRect = CGRectMake(x,y,CropW,CropH);
            }
        }
    }
    [self.viewController presentViewController:imagePickerVc animated:YES completion:nil];
}

-(void)openCamera:(CDVInvokedUrlCommand *)command{
    NSDictionary *options = [command.arguments objectAtIndex:0];
    self.callbackId = command.callbackId;
    self.cameraOptions = options;

    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (authStatus == AVAuthorizationStatusRestricted || authStatus == AVAuthorizationStatusDenied) {
        // 无相机权限 做一个友好的提示
        UIAlertView * alert = [[UIAlertView alloc]initWithTitle:@"无法使用相机" message:@"请在iPhone的""设置-隐私-相机""中允许访问相机" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"设置", nil];
        [alert show];
    } else if (authStatus == AVAuthorizationStatusNotDetermined) {
        // fix issue 466, 防止用户首次拍照拒绝授权时相机页黑屏
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            if (granted) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self takePhoto];
                });
            }
        }];
        // 拍照之前还需要检查相册权限
    } else if ([PHPhotoLibrary authorizationStatus] == 2) { // 已被拒绝，没有相册权限，将无法保存拍的照片
        UIAlertView * alert = [[UIAlertView alloc]initWithTitle:@"无法访问相册" message:@"请在iPhone的""设置-隐私-相册""中允许访问相册" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"设置", nil];
        [alert show];
    } else if ([PHPhotoLibrary authorizationStatus] == 0) { // 未请求过相册权限
        [[TZImageManager manager] requestAuthorizationWithCompletion:^{
            [self takePhoto];
        }];
    } else {
        [self pushImagePickerController];
    }
}

-(void)deleteCache:(CDVInvokedUrlCommand *)command{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    [fileManager removeItemAtPath: [NSString stringWithFormat:@"%@SyanImageCaches", NSTemporaryDirectory()] error:nil];
}

#pragma mark - UIImagePickerController
- (void)takePhoto {
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (authStatus == AVAuthorizationStatusRestricted || authStatus == AVAuthorizationStatusDenied) {
        // 无相机权限 做一个友好的提示
        UIAlertView * alert = [[UIAlertView alloc]initWithTitle:@"无法使用相机" message:@"请在iPhone的""设置-隐私-相机""中允许访问相机" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"设置", nil];
        [alert show];
    } else if (authStatus == AVAuthorizationStatusNotDetermined) {
        // fix issue 466, 防止用户首次拍照拒绝授权时相机页黑屏
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            if (granted) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self takePhoto];
                });
            }
        }];
        // 拍照之前还需要检查相册权限
    } else if ([PHPhotoLibrary authorizationStatus] == 2) { // 已被拒绝，没有相册权限，将无法保存拍的照片
        UIAlertView * alert = [[UIAlertView alloc]initWithTitle:@"无法访问相册" message:@"请在iPhone的""设置-隐私-相册""中允许访问相册" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"设置", nil];
        [alert show];
    } else if ([PHPhotoLibrary authorizationStatus] == 0) { // 未请求过相册权限
        [[TZImageManager manager] requestAuthorizationWithCompletion:^{
            [self takePhoto];
        }];
    } else {
        [self pushImagePickerController];
    }
}

- (UIImagePickerController *)imagePickerVc {
    if (_imagePickerVc == nil) {
        _imagePickerVc = [[UIImagePickerController alloc] init];
        _imagePickerVc.delegate = self;
    }
    return _imagePickerVc;
}

// 调用相机
- (void)pushImagePickerController {
    UIImagePickerControllerSourceType sourceType = UIImagePickerControllerSourceTypeCamera;
    if ([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera]) {
        self.imagePickerVc.sourceType = sourceType;
        [[self viewController] presentViewController:self.imagePickerVc animated:YES completion:nil];
    } else {
        NSLog(@"模拟器中无法打开照相机,请在真机中使用");
    }
}

- (void)imagePickerController:(UIImagePickerController*)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    [picker dismissViewControllerAnimated:YES completion:^{
        NSString *type = [info objectForKey:UIImagePickerControllerMediaType];
        if ([type isEqualToString:@"public.image"]) {

            TZImagePickerController *tzImagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:1 delegate:nil];
            tzImagePickerVc.sortAscendingByModificationDate = NO;
            [tzImagePickerVc showProgressHUD];
            UIImage *image = [info objectForKey:UIImagePickerControllerOriginalImage];

            // save photo and get asset / 保存图片，获取到asset
            [[TZImageManager manager] savePhotoWithImage:image location:NULL completion:^(PHAsset *asset, NSError *error){
                if (error) {
                    [tzImagePickerVc hideProgressHUD];
                    NSLog(@"图片保存失败 %@",error);
                    [self invokeError:error];
                } else {
                    [tzImagePickerVc hideProgressHUD];

                    TZAssetModel *assetModel = [[TZImageManager manager] createModelWithAsset:asset];
                    BOOL isCrop          = [self.cameraOptions sy_boolForKey:@"isCrop"];
                    BOOL showCropCircle  = [self.cameraOptions sy_boolForKey:@"showCropCircle"];
                    NSInteger CropW      = [self.cameraOptions sy_integerForKey:@"CropW"];
                    NSInteger CropH      = [self.cameraOptions sy_integerForKey:@"CropH"];
                    NSInteger circleCropRadius = [self.cameraOptions sy_integerForKey:@"circleCropRadius"];
                    NSInteger   quality = [self.cameraOptions sy_integerForKey:@"quality"];

                    if (isCrop) {
                        TZImagePickerController *imagePicker = [[TZImagePickerController alloc] initCropTypeWithAsset:assetModel.asset photo:image completion:^(UIImage *cropImage, id asset) {
                            [self invokeSuccessWithResult:@[[self handleCropImage:cropImage phAsset:asset quality:quality]]];
                        }];
                        imagePicker.allowPickingImage = YES;
                        if(showCropCircle) {
                            imagePicker.needCircleCrop = showCropCircle; //圆形裁剪
                            imagePicker.circleCropRadius = circleCropRadius; //圆形半径
                        } else {
                            CGFloat x = ([[UIScreen mainScreen] bounds].size.width - CropW) / 2;
                            CGFloat y = ([[UIScreen mainScreen] bounds].size.height - CropH) / 2;
                            imagePicker.cropRect = CGRectMake(x,y,CropW,CropH);
                        }
                        [[self viewController] presentViewController:imagePicker animated:YES completion:nil];
                    } else {
                        [self invokeSuccessWithResult:@[[self handleCropImage:image phAsset:asset quality:quality]]];
                    }
                }
            }];
        }
    }];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [self invokeError:nil];
    if ([picker isKindOfClass:[UIImagePickerController class]]) {
        [picker dismissViewControllerAnimated:YES completion:nil];
    }
}
- (void)invokeError:(NSError *)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (error != nil) {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription] callbackId:self.callbackId];
        } else {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR] callbackId:self.callbackId];
        }
    });
}

- (void)invokeSuccessWithResult:(NSArray *)photos {
    if (self.callbackId != nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:photos] callbackId:self.callbackId];
        });
    }
}


-(void)imagePickerController:(TZImagePickerController *)picker didFinishPickingPhotos:(NSArray<UIImage *> *)photos sourceAssets:(NSArray *)assets isSelectOriginalPhoto:(BOOL)isSelectOriginalPhoto{
}

-(void)imagePickerController:(TZImagePickerController *)picker didFinishPickingGifImage:(UIImage *)animatedImage sourceAssets:(PHAsset *)asset{
    NSLog(@"%@",asset);
}


-(void)imagePickerController:(TZImagePickerController *)picker didFinishPickingPhotos:(NSArray<UIImage *> *)photos sourceAssets:(NSArray *)assets isSelectOriginalPhoto:(BOOL)isSelectOriginalPhoto infos:(NSArray<NSDictionary *> *)infos{
    NSInteger quality  = [self.cameraOptions sy_integerForKey:@"quality"];
    [self handleAssets:assets photos:photos quality:quality isSelectOriginalPhoto:isSelectOriginalPhoto completion:^(NSArray *selecteds) {
        NSLog(@"%@",selecteds);

        if (self.callbackId != nil) {
            [self invokeSuccessWithResult:selecteds];
        }

    } fail:^(NSError *error) {
        [self invokeError:error];
    }];
}

-(void)imagePickerController:(TZImagePickerController *)picker didFinishPickingVideo:(UIImage *)coverImage sourceAssets:(PHAsset *)asset{
    [[TZImageManager manager] getVideoOutputPathWithAsset:asset presetName:AVAssetExportPresetHighestQuality success:^(NSString *outputPath) {
        //TODO
    } failure:^(NSString *errorMessage, NSError *error) {
        NSLog(@"视频导出失败:%@,error:%@",errorMessage, error);
        [self invokeError:error];
    }];
}

- (BOOL)isAssetCanSelect:(PHAsset *)asset {
    BOOL allowPickingGif = [self.cameraOptions sy_boolForKey:@"isGif"];
    BOOL isGIF = [[TZImageManager manager] getAssetType:asset] == TZAssetModelMediaTypePhotoGif;
    if (!allowPickingGif && isGIF) {
        return NO;
    }
    return YES;
}

/// 处理原图数据
- (NSDictionary *)handleOriginalPhotoData:(NSData *)data phAsset:(PHAsset *)phAsset isGIF:(BOOL)isGIF quality:(CGFloat)quality {
    [self createDir];

    NSMutableDictionary *photo  = [NSMutableDictionary dictionary];
    NSString *filename = [NSString stringWithFormat:@"%@%@", [[NSUUID UUID] UUIDString], [phAsset valueForKey:@"filename"]];
    NSString *fileExtension    = [filename pathExtension];
    UIImage *image = nil;
    NSData *writeData = nil;
    NSMutableString *filePath = [NSMutableString string];

    BOOL isPNG = [fileExtension hasSuffix:@"PNG"] || [fileExtension hasSuffix:@"png"];

    if (isGIF) {
        image = [UIImage sd_tz_animatedGIFWithData:data];
        writeData = data;
    } else {
        image = [UIImage imageWithData: data];
        writeData = isPNG ? UIImagePNGRepresentation(image) : UIImageJPEGRepresentation(image, quality/100);
    }

    if (isPNG || isGIF) {
        [filePath appendString:[NSString stringWithFormat:@"%@SyanImageCaches/%@", NSTemporaryDirectory(), filename]];
    } else {
        [filePath appendString:[NSString stringWithFormat:@"%@SyanImageCaches/%@.jpg", NSTemporaryDirectory(), [filename stringByDeletingPathExtension]]];
    }

    [writeData writeToFile:filePath atomically:YES];

    photo[@"uri"]       = filePath;
    photo[@"width"]     = @(image.size.width);
    photo[@"height"]    = @(image.size.height);
    NSInteger size      = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil].fileSize;
    photo[@"size"]      = @(size);
    photo[@"mediaType"] = @(phAsset.mediaType);
    if ([self.cameraOptions sy_boolForKey:@"enableBase64"] && !isGIF) {
        photo[@"base64"] = [NSString stringWithFormat:@"data:image/jpeg;base64,%@", [writeData base64EncodedStringWithOptions:0]];
    }

    return photo;
}

/// 处理视频数据
- (NSDictionary *)handleVideoData:(NSString *)outputPath asset:(PHAsset *)asset coverImage:(UIImage *)coverImage quality:(CGFloat)quality {
    NSMutableDictionary *video = [NSMutableDictionary dictionary];
    video[@"uri"] = outputPath;
    video[@"fileName"] = [asset valueForKey:@"filename"];
    NSInteger size = [[NSFileManager defaultManager] attributesOfItemAtPath:outputPath error:nil].fileSize;
    video[@"size"] = @(size);
    video[@"width"] = @(asset.pixelWidth);
    video[@"height"] = @(asset.pixelHeight);
    video[@"favorite"] = @(asset.favorite);
    video[@"duration"] = @(asset.duration);
    video[@"mediaType"] = @(asset.mediaType);
    video[@"coverUri"] = [self handleCropImage:coverImage phAsset:asset quality:quality][@"uri"];
    return video;
}

/// 创建SyanImageCaches缓存目录
- (BOOL)createDir {
    NSString * path = [NSString stringWithFormat:@"%@SyanImageCaches", NSTemporaryDirectory()];;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isDir;
    if  (![fileManager fileExistsAtPath:path isDirectory:&isDir]) {
        //先判断目录是否存在，不存在才创建
        BOOL res = [fileManager createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:nil];
        return res;
    } else return NO;
}

/// 异步处理获取图片
- (void)handleAssets:(NSArray *)assets photos:(NSArray*)photos quality:(CGFloat)quality isSelectOriginalPhoto:(BOOL)isSelectOriginalPhoto completion:(void (^)(NSArray *selecteds))completion fail:(void(^)(NSError *error))fail {
    NSMutableArray *selectedPhotos = [NSMutableArray array];

    [assets enumerateObjectsUsingBlock:^(PHAsset * _Nonnull asset, NSUInteger idx, BOOL * _Nonnull stop) {
        if (asset.mediaType == PHAssetMediaTypeVideo) {
            [[TZImageManager manager] getVideoOutputPathWithAsset:asset presetName:AVAssetExportPresetHighestQuality success:^(NSString *outputPath) {
                [selectedPhotos addObject:[self handleVideoData:outputPath asset:asset coverImage:photos[idx] quality:quality]];
                if ([selectedPhotos count] == [assets count]) {
                    completion(selectedPhotos);
                }
                if (idx + 1 == [assets count] && [selectedPhotos count] != [assets count]) {
                    fail(nil);
                }
            } failure:^(NSString *errorMessage, NSError *error) {

            }];
        } else {
            BOOL isGIF = [[TZImageManager manager] getAssetType:asset] == TZAssetModelMediaTypePhotoGif;
            if (isGIF || isSelectOriginalPhoto) {
                [[TZImageManager manager] requestImageDataForAsset:asset completion:^(NSData *imageData, NSString *dataUTI, UIImageOrientation orientation, NSDictionary *info) {
                    [selectedPhotos addObject:[self handleOriginalPhotoData:imageData phAsset:asset isGIF:isGIF quality:quality]];
                    if ([selectedPhotos count] == [assets count]) {
                        completion(selectedPhotos);
                    }
                    if (idx + 1 == [assets count] && [selectedPhotos count] != [assets count]) {
                        fail(nil);
                    }
                } progressHandler:^(double progress, NSError *error, BOOL *stop, NSDictionary *info) {

                }];
            } else {
                [selectedPhotos addObject:[self handleCropImage:photos[idx] phAsset:asset quality:quality]];
                if ([selectedPhotos count] == [assets count]) {
                    completion(selectedPhotos);
                }
            }
        }
    }];
}

/// 处理裁剪图片数据
- (NSDictionary *)handleCropImage:(UIImage *)image phAsset:(PHAsset *)phAsset quality:(CGFloat)quality {
    [self createDir];

    NSMutableDictionary *photo  = [NSMutableDictionary dictionary];
    NSString *filename = [NSString stringWithFormat:@"%@%@", [[NSUUID UUID] UUIDString], [phAsset valueForKey:@"filename"]];
    NSString *fileExtension    = [filename pathExtension];
    NSMutableString *filePath = [NSMutableString string];
    BOOL isPNG = [fileExtension hasSuffix:@"PNG"] || [fileExtension hasSuffix:@"png"];

    if (isPNG) {
        [filePath appendString:[NSString stringWithFormat:@"%@SyanImageCaches/%@", NSTemporaryDirectory(), filename]];
    } else {
        [filePath appendString:[NSString stringWithFormat:@"%@SyanImageCaches/%@.jpg", NSTemporaryDirectory(), [filename stringByDeletingPathExtension]]];
    }

    NSData *writeData = isPNG ? UIImagePNGRepresentation(image) : UIImageJPEGRepresentation(image, quality/100);
    [writeData writeToFile:filePath atomically:YES];

    photo[@"uri"]       = filePath;
    photo[@"width"]     = @(image.size.width);
    photo[@"height"]    = @(image.size.height);
    NSInteger size = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil].fileSize;
    photo[@"size"] = @(size);
    photo[@"mediaType"] = @(phAsset.mediaType);
    if ([self.cameraOptions sy_boolForKey:@"enableBase64"]) {
        photo[@"base64"] = [NSString stringWithFormat:@"data:image/jpeg;base64,%@", [writeData base64EncodedStringWithOptions:0]];
    }

    return photo;
}

@end
