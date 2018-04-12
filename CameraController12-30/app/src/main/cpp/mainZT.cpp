#include <opencv2/opencv.hpp>
#include <iostream>
#include <vector>
#include <string>
#include <fstream>

#include "kcftracker.hpp"
#include "selectRoi.h"

using namespace cv;
using namespace std;

int main(int argc, char **argv)
{
    bool HOG = true;
    bool FIXEDWINDOW = false;
    bool MULTISCALE = false;
    bool SILENT = true;
    bool LAB = false;

    VideoCapture my_cap(0);
//    my_cap.open("/home/pc/DeepLearning/BitbucketProject/CuiZhouSecurity/Application/EPolice/data/videos/33.241.138.58");
    if(!my_cap.isOpened())
    {
        cerr << "Open usb camera Failed !" << endl;
        return 0;
    }

    int64 tic, toc;
    double time = 0;
    bool show_visualization = true;

    std::string kernel_type = "gaussian";//gaussian polynomial linear
    std::string feature_type = "hog";//hog gray
    KCFTracker kcf_tracker(HOG, FIXEDWINDOW, MULTISCALE, LAB);

    bool initFlag = false;
    int frameNum = 0;
    cv::Rect resultRect;

    while(1)
    {
        cv::Mat image;
        my_cap >> image;
//        cv::resize(image, image, cv::Size(image.cols/4, image.rows/4));

        if (!initFlag)
        {
            bool selectFlag = getMyRect(image);
            if(!selectFlag)
            {
                continue;
            }
            kcf_tracker.init(rectSelect, image);
            resultRect = rectSelect;

            initFlag = true;
            frameNum++;
        }
        else
        {
            tic = cv::getTickCount();

            resultRect = kcf_tracker.update(image);
            frameNum++;

            toc = cv::getTickCount() - tic;
            time += toc;
        }


        if (show_visualization)
        {
//            cv::putText(image, to_string(frameNum), cv::Point(20, 40), 6, 1,
//                        cv::Scalar(0, 255, 255), 2);
            cv::rectangle(image, resultRect, cv::Scalar(0, 255, 255), 2);
            cv::imshow("follower", image);

            char key = cv::waitKey(1);
            if(key == 27 || key == 'q' || key == 'Q')
                break;
        }
    }

    time = time / getTickFrequency();
    double fps = double(frameNum) / time;
    printf("FPS : %4.2f\n", fps);

    cv::destroyAllWindows();
    return 0;
}
