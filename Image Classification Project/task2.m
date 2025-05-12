

inputImage = imread('testimg.jpeg'); %Loads the image

% ensures and checks for greyscale
if size(inputImage, 3) == 3
    inputImage = rgb2gray(inputImage); % converts if its a non-greyscale image
end

%parameters
w = 3; % gaussian filter size
sigma = 1.5; % Standard deviation
k = 0.04; % Harris constant
p = 0.01; % Threshold for the corners

% Calls the function made
corners = harrisCornerDetector(inputImage, w, sigma, k, p);

% Displays the corners on top of the original image
imshow(inputImage);
hold on;
plot(corners(:,2), corners(:,1), 'r*'); % Uses red stars 
title('Detected Corners');
hold off;

% Harris Corner Detector Function
function corners = harrisCornerDetector(inputImage, w, sigma, k, p)
    % the Sobel filters to compute gradient
    sobelX = [-1 0 1; -2 0 2; -1 0 1];
    sobelY = [-1 -2 -1; 0 0 0; 1 2 1];
    Ix = imfilter(double(inputImage), sobelX, 'replicate');
    Iy = imfilter(double(inputImage), sobelY, 'replicate');

    % computes the gradient products
    Ix2 = Ix.^2;
    Iy2 = Iy.^2;
    Ixy = Ix .* Iy;

    % Applies the gaussian filter 
    gaussianFilter = fspecial('gaussian', [w w], sigma);
    A = imfilter(Ix2, gaussianFilter, 'replicate');
    B = imfilter(Ixy, gaussianFilter, 'replicate');
    C = imfilter(Iy2, gaussianFilter, 'replicate');

    % Computes the harris response
    R = (A .* C - B.^2) - k * (A + C).^2;

    % Threshold for the corner candidates
    R_threshold = p * max(R(:));
    cornerCandidates = (R > R_threshold);

    % To find the local maxima
    localMax = imdilate(R, strel('square', 3));
    cornersBinary = (R == localMax) & cornerCandidates;

    % finds the corner coordinates
    [row, col] = find(cornersBinary);
    corners = [row, col];
end
