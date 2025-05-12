% Load the map and set up the start and finish points
map = im2bw(imread('random_map.bmp')); % Load the map as a binary image
start = [1, 1]; % Starting point of the path
finish = [500, 500]; % Target point for the path
mapSize = size(map); % Dimensions of the map

% Define parameters for the Genetic Algorithm
populationSize = 100; % How many candidate paths to explore at a time
numGenerations = 200; % How many iterations to improve the paths
noOfPointsInSolution = 10; % Number of points between start and finish
mutationRate = 0.05; % How often random changes occur in a path
crossoverRate = 0.8; % Likelihood of combining two paths

% scales points
lowerBound = 0;
upperBound = 1;

% asks the user their selection preferences
disp('Pick a selection method: 0 = RWS, 1 = Tournament, 2 = Rank-based');
selectionMethod = input('Selection method: ');

disp('Pick a crossover method: 0 = Single-Point, 1 = Two-Point');
crossoverMethod = input('Crossover method: ');

disp('Pick a mutation method: 0 = Uniform, 1 = Gaussian');
mutationMethod = input('Mutation method: ');

population = lowerBound + (upperBound - lowerBound) * rand(populationSize, 2 * noOfPointsInSolution);


globalBestSolution = [];
globalBestFitness = inf; % starts with large fitness value

% main loop
for generation = 1:numGenerations
    % evaluates how good a path is
    fitness = zeros(populationSize, 1);
    for i = 1:populationSize
        fitness(i) = calculateFitness(population(i, :), map, start, finish);
    end

    % updates best solution if better one is found
    [currentBestFitness, bestIdx] = min(fitness);
    if currentBestFitness < globalBestFitness
        globalBestFitness = currentBestFitness;
        globalBestSolution = population(bestIdx, :);
    end

    % combines and mutates paths
    newPopulation = [];
    while size(newPopulation, 1) < populationSize
        % chooses 2 parents based on selection
        if selectionMethod == 0
            parent1 = rouletteWheelSelection(population, fitness);
            parent2 = rouletteWheelSelection(population, fitness);
        elseif selectionMethod == 1
            parent1 = tournamentSelection(population, fitness, 3);
            parent2 = tournamentSelection(population, fitness, 3);
        else
            parent1 = rankSelection(population, fitness);
            parent2 = rankSelection(population, fitness);
        end

        % combines parents
        if crossoverMethod == 0
            [child1, child2] = singlePointCrossover(parent1, parent2);
        else
            [child1, child2] = twoPointCrossover(parent1, parent2);
        end

        % applys random changes
        if mutationMethod == 0
            child1 = mutateUniform(child1, mutationRate, lowerBound, upperBound);
            child2 = mutateUniform(child2, mutationRate, lowerBound, upperBound);
        else
            child1 = mutateGaussian(child1, mutationRate, lowerBound, upperBound);
            child2 = mutateGaussian(child2, mutationRate, lowerBound, upperBound);
        end

       
        newPopulation = [newPopulation; child1; child2];
    end

    % Replace the old population with the new one
    population = newPopulation(1:populationSize, :);

    % Every 10 generations, show the current best path
    if mod(generation, 10) == 0
        path = [start; [globalBestSolution(1:2:end)' * size(map, 1), globalBestSolution(2:2:end)' * size(map, 2)]; finish];
        clf;
        imshow(map);
        rectangle('position', [1 1 size(map) - 1], 'edgecolor', 'k');
        line(path(:, 2), path(:, 1), 'Color', 'r', 'LineWidth', 2);
        title(['Generation ', num2str(generation), ' Best Fitness: ', num2str(globalBestFitness)]);
        pause(0.1);
    end
end

% displays best length
path = [start; [globalBestSolution(1:2:end)' * size(map, 1), globalBestSolution(2:2:end)' * size(map, 2)]; finish];
clf;
imshow(map);
rectangle('position', [1 1 size(map) - 1], 'edgecolor', 'k');
line(path(:, 2), path(:, 1), 'Color', 'r', 'LineWidth', 2);
title(['Final Best Path with Length: ', num2str(globalBestFitness)]);

% calculates how good a path is
function fitness = calculateFitness(solution, map, start, finish)
    path = [start; [solution(1:2:end)' * size(map, 1), solution(2:2:end)' * size(map, 2)]; finish];
    fitness = 0;

    % Check the length and validity of each segment
    for i = 1:size(path, 1) - 1
        segment = path(i:i+1, :);
        x = linspace(segment(1, 1), segment(2, 1), 500); 
        y = linspace(segment(1, 2), segment(2, 2), 500);

        % ensures points stay within map
        x = max(1, min(size(map, 1), round(x)));
        y = max(1, min(size(map, 2), round(y)));

        % penalise paths that pass through obstacles
        if any(map(sub2ind(size(map), x, y)))
            fitness = fitness + 1e7; 
        end

        % add segment length to fitness
        fitness = fitness + sqrt(sum((segment(2, :) - segment(1, :)).^2));
    end

    % penalises sharp turns
    for i = 2:size(path, 1) - 1
        v1 = path(i, :) - path(i - 1, :);
        v2 = path(i + 1, :) - path(i, :);
        angle = acos(dot(v1, v2) / (norm(v1) * norm(v2))); % calculates turn angle
        fitness = fitness + abs(angle) * 100; % penalty for sharp turns
    end
end


function selected = rouletteWheelSelection(population, fitness)
    probabilities = 1 ./ fitness; % inverts fitness
    probabilities = probabilities / sum(probabilities); % normalise to sum to 1
    cumulativeProb = cumsum(probabilities); % creates a probability array
    r = rand(); % random number 
    selected = population(find(cumulativeProb >= r, 1), :); % selects based on probability
end

% Tournament Selection
function selected = tournamentSelection(population, fitness, tournamentSize)
    indices = randperm(size(population, 1), tournamentSize); 
    [~, bestIdx] = min(fitness(indices)); % finds fittest
    selected = population(indices(bestIdx), :); % returns winner
end

% Rank-Based Selection
function selected = rankSelection(population, fitness)
    [~, rank] = sort(fitness); % sorts individuals by their fitness
    probabilities = rank / sum(rank); % higher rank = higher prob
    cumulativeProb = cumsum(probabilities); 
    r = rand(); % random number to pick an individual
    selected = population(find(cumulativeProb >= r, 1), :); % selects based on rank
end

% Single-Point Crossover
function [child1, child2] = singlePointCrossover(parent1, parent2)
    crossoverPoint = randi(length(parent1)); % picks a point to split
    child1 = [parent1(1:crossoverPoint), parent2(crossoverPoint+1:end)];
    child2 = [parent2(1:crossoverPoint), parent1(crossoverPoint+1:end)]; 
end

% Two-Point Crossover
function [child1, child2] = twoPointCrossover(parent1, parent2)
    points = sort(randperm(length(parent1), 2)); % picks 2 points
    child1 = [parent1(1:points(1)), parent2(points(1)+1:points(2)), parent1(points(2)+1:end)];
    child2 = [parent2(1:points(1)), parent1(points(1)+1:points(2)), parent2(points(2)+1:end)];
end

% Uniform Mutation
function mutated = mutateUniform(individual, mutationRate, lowerBound, upperBound)
    for i = 1:length(individual)
        if rand() < mutationRate
            individual(i) = lowerBound + (upperBound - lowerBound) * rand(); % randomly changes a point
        end
    end
    mutated = individual; % returns mutated individual
end

% Gaussian Mutation
function mutated = mutateGaussian(individual, mutationRate, lowerBound, upperBound)
    for i = 1:length(individual)
        if rand() < mutationRate
            individual(i) = individual(i) + randn() * 0.1;
            individual(i) = max(lowerBound, min(upperBound, individual(i))); % ensures individual is kept within the bounds
        end
    end
    mutated = individual; % returns the mutated individual
end
