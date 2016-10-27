addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/');


file_ID=fopen('random.txt');  %  initial weights of 1000 trials.
random= textscan(file_ID,'%f');
random1 = reshape(random{1},1,10000);
edges = linspace(-10,10,1000);
figure()

h1 = histogram(random1,edges);

a = h1.Values