function a = gregr(vals)
% general linear regression
%
% a = gregr(vals)
%         vals contains lines with the x-values and the y value:
%                   (x1 x2 ... xn y)
%
%         a is the best resulting linear approximatio
%                   vals(:, 1:(n-1))*a approx. vals(:,n)
%                   with n = size(vals, 2)

[rows, cols] = size(vals);

a = vals(:,1:(cols-1)) \ vals(:,cols);

%A = zeros(cols-1);
%v = zeros(cols-1, 1);
%
%for j=1:rows
%    d = vals(j, 1:(cols-1));
%    A = A + d'*d;
%    v = v + vals(j,cols)*d';
%end;
%
%A
%v
%
%a = A\v;
