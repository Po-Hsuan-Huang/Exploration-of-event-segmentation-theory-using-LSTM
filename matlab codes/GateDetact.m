close all
clear
restoredefaultpath


dimension = 3;

if dimension == 1 
    
    % for 1D bouncing ball world
    addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D');
    file_ID=fopen('input.txt');
    input = textscan(file_ID,'%f');
    file_ID=fopen('gatehist.txt');
    hist = textscan(file_ID,'%f');

    dataSize =size(hist{1},1);
    truncation  = 200; % the length of the duration of listening.
    neurons_num = 15 ; % for LSTM 1-1-1
    % neurons_num = 28 ; % for LSTM 1-2-1
    steps_num = dataSize/neurons_num;
    matrix = reshape(hist{1},neurons_num,steps_num); % (hist{1},15,100) for LSTM 1-1-1 
elseif dimension ==3
     % for 3D bouncing ball world
%     addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/2DWorld/LSTM 3-1-3');
      addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/2DWorld');

    file_ID=fopen('input.txt');
    input = textscan(file_ID,'[%f,%f,%f]');
    file_ID=fopen('gatehist.txt');
    hist = textscan(file_ID,'%f');

    dataSize =size(hist{1},1);
    truncation  = 200; % the length of the duration of listening.
    neurons_num = 45 ; % for LSTM 3-3-3
%     neurons_num = 19 ; % for LSTM 3-1-3
    steps_num = dataSize/neurons_num;
    matrix = reshape(hist{1},neurons_num,steps_num); % (hist{1},15,100) for LSTM 1-1-1 

end
    
%%
matrix1 = matrix(:,1:truncation);
matrix2 = matrix(:,truncation+1:steps_num);


% Location contour
% boundary = double(input{1}((end-truncation)+1 :end)'==1)- double(input{1}((end-truncation)+1 :end)'==-1);
% Z = repmat(boundary,15,1);

figure()
s = surf(matrix,'EdgeAlpha',0.1);
colormap hsv;
c = colorbar('FontSize',12);
c.Box = 'off';
c.Label.String = ' object velocity';
c.Limits = [-1,1];
axis tight
hold on
% h = contour3(Z,[1,1],'LineWidth',1);

hold off
title('gate boundary prediction')
ylabel('cell ID')
xlabel('time steps')

