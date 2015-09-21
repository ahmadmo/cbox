package org.telegram.bot.cbox;

import static org.telegram.bot.messaging.Emoticons.*;

/**
 * @author ahmad
 */
public final class Constants {

    private Constants() {
    }

    public static final String PHOTO_ICON = "\ud83d\uddfb";
    public static final String VIDEO_ICON = "\ud83c\udfac";
    public static final String AUDIO_ICON = "\ud83c\udfa4";
    public static final String MUSIC_ICON = "\ud83c\udfb5";
    public static final String TEXT_ICON = "\ud83d\udcdd";
    public static final String DOCUMENT_ICON = "\ud83d\udccb";

    public static final String EMPTY_LIST_MESSAGE = "Empty list " + NOT_SURE_1 + "\n\nTry to send me some files ...";
    public static final String CHOOSE_CONTEXT_MESSAGE = "Choose context: ";
    public static final String MY_FILES = "My files";
    public static final String GROUP_FILES = "Group files";
    public static final String INVALID_CONTEXT_MESSAGE = "Invalid context chosen. %s";
    public static final String INVALID_FILE_NAME_MESSAGE = "Invalid file name. %s";
    public static final String FILE_NOT_FOUND_MESSAGE = "File not found. " + HIDDEN_FACE + NOT_SURE_2;
    public static final String FILE_OPTIONS_MESSAGE = "Choose one of the following options:";
    public static final String GET_FILE = "get";
    public static final String FILE_METADATA = "metadata";
    public static final String SHARE_FILE = "share";
    public static final String DELETE_FILE = "delete";
    public static final String DELETE_ALL = "delete all";
    public static final String INVALID_OPTION_MESSAGE = "Invalid option. %s";
    public static final String LINK_MESSAGE = "%s\n\nThis link will expire after 3 days.";
    public static final String INVALID_LINK_MESSAGE = "The link is invalid or expired.";
    public static final String DELETE_CONFIRMATION = "Yes, I am totally sure.";
    public static final String DELETE_CONFIRMATION_GROUP = "Yes, We are totally sure.";
    public static final String CONFIRM_DELETE_FILE_MESSAGE = "Are you sure want to delete \"%s\"? " + NOT_SURE_1 +
            "\n\nSend \"" + DELETE_CONFIRMATION + "\" to confirm you really want to delete this file.";
    public static final String CONFIRM_DELETE_FILE_GROUP_MESSAGE = "Are you sure want to delete \"%s\"? " + NOT_SURE_1 +
            "\n\nReply \"" + DELETE_CONFIRMATION_GROUP + "\" to this message to confirm you really want to delete this file.";
    public static final String CONFIRM_DELETE_ALL_MESSAGE = "Are you sure want to delete all files? " + NOT_SURE_2 +
            "\n\nSend \"" + DELETE_CONFIRMATION + "\" to confirm you really want to delete them.";
    public static final String CONFIRM_DELETE_ALL_GROUP_MESSAGE = "Are you sure want to delete all files? " + NOT_SURE_2 +
            "\n\nReply \"" + DELETE_CONFIRMATION_GROUP + "\" to this message to confirm you really want to delete them.";
    public static final String INVALID_DELETE_CONFIRMATION_MESSAGE = "%s Please enter the confirmation text exactly like this:" +
            "\n" + DELETE_CONFIRMATION + "\n\nType /cancel to cancel the operation.";
    public static final String INVALID_DELETE_CONFIRMATION_GROUP_MESSAGE = "%s Please enter the confirmation text exactly like this:" +
            "\n" + DELETE_CONFIRMATION_GROUP + "\n\nType /cancel to cancel the operation.";
    public static final String N_MORE_TO_GO = "OK! %d more to go ... " + COOL;
    public static final String ONE_MORE_CONFIRMATION = "Hmmmm! Still need one more confirmation. " + HEH;
    public static final String DELETE_COMPLETED_MESSAGE = "Success! The file was deleted.";
    public static final String DELETE_ALL_COMPLETED_MESSAGE = "Success! All files were deleted.";
    public static final String CONFIRMATION_REJECTED = "Please cancel the current operation and then try again!";
    public static final String CONFIRMATION_ENDED_MESSAGE = "Oops! Confirmation is ended or canceled.";
    public static final String SEARCH_HELP_MESSAGE = "You may search for a file, For example:" +
            "\n\nsearch by name:\n/search %s\n\nsearch by date:\n/search today" +
            "\n\nsearch for a period of time:\n/search %s\n/search yesterday - today";
    public static final String SEARCH_DID_NOT_MATCH_MESSAGE = "Your search \"%s\" did not match any files. %s";
    public static final String SET_FILTERS_HELP_MESSAGE = "1 = Photo\n2 = Audio\n3 = Video\n4 = Document\n\nExample:\n/setFilters 1234" +
            "\n\nNote! The above example makes it to include all file types in the results and is totally equal to the command /noFilters." +
            "\n\nCurrent filters is: %n%s";
    public static final String INVALID_FILTERS_MESSAGE = "Invalid filters. %s\n\nPlease try again ...";
    public static final String FILTERS_UPDATED_MESSAGE = "Success! Filters updated. %s\n\nCurrent filters is: %n%s";
    public static final String NO_FILTERS_MESSAGE = "Success! There would be no filters in the results. " + TONGUE;
    public static final String HELP_MESSAGE = "Here is what I can do for you:\n" +
            "\n/list - use this command to see the list of your files." +
            "\n/search - are you looking for a file but can not find it?\nOK! use this command to search for a file by its name or date." +
            "\n/setFilters - wanna see specific types of files?" +
            "\n/noFilters - use this command to include all file types in the results." +
            "\n/help - \"I don't always use recursion\nbut when I do, I don't always use recursion.\" " + COOL +
            "\n/cancel - cancel the current operation.";
    public static final String CANCEL_COMMAND_MESSAGE = "The command \"%s\" has been cancelled. Anything else I can do for you? %s" +
            "\n\nSend /help for a list of commands.";
    public static final String NO_ACTIVE_COMMAND_MESSAGE = "No active command to cancel. I wasn't doing anything anyway. " + ZZZ;
    public static final String ERROR_MESSAGE = "Internal Server Error! %s\n\nPlease try again ...";

    public static final String CHOOSE_CONTEXT_KEYBOARD = "{\"keyboard\":[[\"" + MY_FILES + "\"],[\"" + GROUP_FILES
            + "\"]],\"resize_keyboard\":true,\"one_time_keyboard\":true,\"selective\":true}";
    public static final String FILE_OPTIONS_KEYBOARD = "{\"keyboard\":[[\"" + GET_FILE + "\"],[\"" + FILE_METADATA
            + "\"],[\"" + SHARE_FILE + "\"],[\"" + DELETE_FILE
            + "\"]],\"resize_keyboard\":true,\"selective\":true}";
    public static final String HIDE_KEYBOARD = "{\"hide_keyboard\":true,\"selective\":true}";

}
