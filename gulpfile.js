var gulp = require('gulp');
var sass = require('gulp-sass');
var concat = require('gulp-concat');

var config = {
    bootstrapDir: './assets/bower_components/bootstrap-sass',
    faDir: './assets/bower_components/font-awesome',
    emojioneDir: './assets/bower_components/emojione',
    publicDir: './assets/public',
    webappDir: './src/main/resources/public/assets'
};

gulp.task('css', function() {
    return gulp.src('./assets/css/application.scss')
    .pipe(sass({
        includePaths: [config.bootstrapDir + '/assets/stylesheets', config.faDir + '/scss'],
    }))
    //.pipe(concat('emojione.sprites.css'))
    .pipe(gulp.dest(config.publicDir + '/css'))
    .pipe(gulp.dest(config.webappDir + '/css'));
});

gulp.task('fonts', function() {
    return gulp.src(config.bootstrapDir + '/assets/fonts/**/*')
    	.pipe(gulp.dest(config.publicDir + '/fonts'));
});
//fixme not working
gulp.task('emojione', function() {
    return gulp.src(config.emojioneDir + '/assets/sprites/*.png')
    	.pipe(gulp.dest(config.publicDir + '/assets/css'));
});
//fixme not working
gulp.task('emojioneJs', function() {
    return gulp.src(config.emojioneDir + '/lib/js/emojione*.js')
    	.pipe(gulp.dest(config.publicDir + '/assets/js'));
});

gulp.task('default', ['css', 'fonts']);
