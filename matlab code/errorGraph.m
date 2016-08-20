clear;
pdcs = load('pdcs.txt');
errors = load('errors.txt');
locs = load('locs.txt');
dots = cat(2,locs(end-1000:end),pdcs(end-1000:end));
%% predction vs location
s = locs(end-999:end);

figure()
% scatter(dots(:,1),dots(:,2),'b.');
% x = 1:1:length(pdcs);
% plot(x,locs','bo',x,pdcs','rx');
x = 1:1:1000;
plot(locs(end-999:end),);

xlabel('location');
ylabel('predicted loc');

%% prediction error vs time


% F=figure(3);
% maxfig(F,0) % Restores figure F if F is maximized
% 
% len=  length(errors);
% k=0;
% modus = 10;
% x = zeros(1,len/modus);
% for i = 1:modus:len
%     k= k+1;
%     x(k)= errors(i);
% end
% axis =1:1:len/modus;
% plot(axis,x);
% title('the error over time')
% xlabel('time')
% ylabel('error')
% 
