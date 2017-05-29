function [m,b] = regr(v)
% [m,b] = regr(v)
%   ergebnis y = m*x + b
%
% bitte v mit 2 spalten.
% lineare regression sucht nach ausgleichsgeraden ...

rows = size(v, 1);

v = [v(:,1) v(:,1).^2 v(:,2) v(:,1).*v(:,2)];
X = sum(v);

M = [rows X(1); X(1) X(2)];
z = [X(3); X(4)];

%solve M*x = z
x = M\z;

b = x(1);
m = x(2);


 