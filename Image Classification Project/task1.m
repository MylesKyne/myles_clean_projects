
inputImage = imread('testimg.jpeg'); %Loads the input image

% checks and ensure image is greyscale
if size(inputImage, 3) == 3
    inputImage = rgb2gray(inputImage); % Convert to grayscale if it's a color image
end

% converts image to make it easier to manipulate
inputImage = im2double(inputImage);

% ensures divisible by 8
[height, width] = size(inputImage);
height = floor(height / 8) * 8;
width = floor(width / 8) * 8;
inputImage = imresize(inputImage, [height, width]);

% Calls HOG extractor function i made
[cellHistograms, extractedHOGFeatures] = extractHOGFeatures(inputImage);

% Visualise as histograms
visualizeHOG(inputImage, cellHistograms);


% HOG Feature Extraction Function
function [cellHistograms, extractedHOGFeatures] = extractHOGFeatures(inputImage)
    % Sobel filters to compute gradients
    sobelX = [-1 0 1; -2 0 2; -1 0 1];
    sobelY = [-1 -2 -1; 0 0 0; 1 2 1];
    Gx = imfilter(inputImage, sobelX, 'replicate');
    Gy = imfilter(inputImage, sobelY, 'replicate');

    % finds gradient magnitude as well as the orientation
    magnitude = sqrt(Gx.^2 + Gy.^2);
    orientation = atan2d(Gy, Gx);
    orientation(orientation < 0) = orientation(orientation < 0) + 180;

    % paramters, made to be easy to understand
    cellSize = 8;
    binSize = 9;
    binEdges = linspace(0, 180, binSize + 1);
    blockSize = 2; % 2x2 cells

    % computes the histogram for each cell
    [height, width] = size(inputImage);
    numCellsX = width / cellSize;
    numCellsY = height / cellSize;
    cellHistograms = zeros(numCellsY, numCellsX, binSize);

    for i = 1:numCellsY
        for j = 1:numCellsX
            % Extract cell region
            cellMagnitude = magnitude((i-1)*cellSize+1:i*cellSize, (j-1)*cellSize+1:j*cellSize);
            cellOrientation = orientation((i-1)*cellSize+1:i*cellSize, (j-1)*cellSize+1:j*cellSize);

            % Compute histogram for the cell
            cellHistogram = zeros(1, binSize);
            for k = 1:numel(cellOrientation)
                % finds the appropriate bin
                [~, binIdx] = min(abs(cellOrientation(k) - binEdges(1:end-1)));
                cellHistogram(binIdx) = cellHistogram(binIdx) + cellMagnitude(k);
            end
            cellHistograms(i, j, :) = cellHistogram;
        end
    end

    % normalises the histograms in case of overlapping
    numBlocksX = numCellsX - blockSize + 1;
    numBlocksY = numCellsY - blockSize + 1;
    blockFeatures = [];

    for i = 1:numBlocksY
        for j = 1:numBlocksX
            % Extract 2x2 block of histograms
            block = cellHistograms(i:i+1, j:j+1, :);
            block = block(:); % flattens into vector

            % normalise
            block = block / sqrt(sum(block.^2) + 1e-6);
            blockFeatures = [blockFeatures; block];
        end
    end

    % Return the HOG feature vector and cell histograms
    extractedHOGFeatures = blockFeatures;
end


% Function to Visualize HOG as Histograms
function visualizeHOG(inputImage, cellHistograms)
    % Parameters
    cellSize = 8;
    binSize = size(cellHistograms, 3);
    binEdges = linspace(0, 180, binSize + 1);

    % displays the greyscale image next to histogram
    figure;
    subplot(1, 2, 1);
    imshow(inputImage);
    title('Input Image');

    % histogram display
    subplot(1, 2, 2);
    hold on;
    title('Histogram of Oriented Gradients (HOG)');
    xlabel('Orientation Bins');
    ylabel('Magnitude');
    
    % Average histogram across all cells
    avgHistogram = mean(reshape(cellHistograms, [], binSize), 1);

    % Plots the histogram
    bar(binEdges(1:end-1), avgHistogram, 'FaceColor', [0.2 0.2 0.8]);
    xlim([0 180]);
    xticks(binEdges(1:end-1));
    grid on;
    hold off;
end
